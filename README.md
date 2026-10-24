# InfraPilot — Developer Build Guide v2.1 Platform

> **A post-development developer platform**: Paste a GitHub or GitLab repo link and InfraPilot automatically analyzes the repo, executes CI/CD, runs multi-tier tests, enforces shift-left security quality gates, projects FinOps cloud costs, deploys via Terraform to AWS, and visualizes live SRE metrics in Grafana — with a replaceable bundled full-stack demo app.

---

## 1. Product Concept & Architecture

InfraPilot eliminates the barrier between finishing an application and production deployment. Developers provide a repository URL, and InfraPilot manages the entire pipeline without requiring developers to write complex CI pipelines or Terraform code themselves.

```
Developer
 | repo URL (GitHub / GitLab)
 v
+--------------------+      REST      +--------------------+      trigger      +--------------------------+
|  Vaadin Dashboard  | -------------> |  Spring Boot API   | ----------------> | Jenkins / GitHub Actions |
|  (login, deploy,   | <------------- |  (auth, projects,  | <---------------- |  / GitLab CI pipeline    |
|  pipeline,security,|   status/push  |   runs, scans, cost)|    callbacks      +------------+-------------+
|  cost, monitor)    |                +---------+----------+                               |
+--------------------+                          |                                          v
                                                | PostgreSQL
                                                |   Checkout > Build > Unit tests > API tests > Sonar
                                                |   > Gitleaks > Dependency-Check > Checkov > Docker build
                                                |   > Trivy > Infracost > Terraform plan > APPROVAL
                                                |   > Terraform apply > Ansible/Compose deploy
                                                |   > Smoke test > k6 load test
                                                v
                               +----------------------- AWS (ap-south-1) -----------------------+
                               | EC2: app containers + node-exporter + cAdvisor                 |
                               | Prometheus + Grafana + Alertmanager (same host or separate)    |
                               | S3 (Terraform state) + DynamoDB (lock) + ECR (images)          |
                               +----------------------------------------------------------------+
```

---

## 2. Tool Stack Mapped to DevOps Stages

| Stage | Tool | What It Proves | Fail Rule |
|---|---|---|---|
| **Source** | GitHub, GitLab, Git | Branching strategies, webhook automation, PR checks | - |
| **Build** | Maven 3.9 / Docker | Multi-stage reproducible container builds | Build error = **STOP** |
| **Unit Test** | JUnit 5 + JaCoCo | Testing discipline, mocking, code coverage enforcement | Any test failure = **STOP** |
| **API Test** | Newman (Postman CLI) | End-to-end contract and flow validation | Failed assertion = **STOP** |
| **Load Test** | k6 | Performance baselining (20 VUs, 30s) | p95 latency > 500ms = **WARN** |
| **Code Quality** | SonarQube | Static code analysis, security hotspots, debt metrics | Quality gate fail = **WARN** |
| **Secret Scan** | Gitleaks | Shift-left detection of API keys and AWS tokens | Any detected secret = **STOP** |
| **Dependency Scan** | Trivy fs / OWASP Dep-Check | Supply chain security, known CVE scanning | CRITICAL vulnerability = **STOP** |
| **IaC Scan** | Checkov | Terraform policy-as-code security posture | CRITICAL/HIGH = **STOP** |
| **Image Scan** | Trivy | Container image vulnerability evaluation | CRITICAL CVE = **STOP** |
| **FinOps Cost** | Infracost | Pre-deployment infrastructure cost visibility | Exceeds budget cap = **WARN** |
| **Provisioning** | Terraform | Declarative Infrastructure as Code with remote S3 state lock | Plan error = **STOP** |
| **Configuration** | Ansible / `user_data.sh` | Idempotent host configuration and Docker runtime setup | Task failure = **STOP** |
| **Deployment** | Docker Compose on EC2 | Containerized microservice delivery | Smoke test fail = **ROLLBACK** |
| **Observability** | Prometheus, Grafana, cAdvisor, Node-Exporter | 4 Golden Signals, host telemetry, JVM internals | Alert rules |
| **Alerting** | Alertmanager | Automated incident response flow (Slack/Webhook) | - |

---

## 3. Repository Structure

```
infrapilot/
|-- dashboard/                      # Vaadin 24 UI (Java 17)
|   `-- src/main/java/com/infrapilot/ui/
|       |-- MainLayout.java
|       `-- views/ LoginView, DashboardView, NewRunView, PipelineView,
|                  SecurityView, TestsView, CostView, MonitorView, SettingsView
|-- api/                            # Spring Boot 3 REST API
|   `-- src/main/java/com/infrapilot/
|       |-- web/ RunController, ProjectController, CallbackController, AuthController, HealthCheckController
|       |-- service/ RunService, RepoAnalyzer, JenkinsClient, GitHubClient, GitLabClient, CostService
|       |-- domain/ Project, Run, StageResult, ScanResult, CostEstimate, TestResult, AuditEvent
|       |-- security/ JwtFilter, SecurityConfig, JwtService
|       `-- resources/db/migration/ V1__init.sql, V2__results.sql
|-- demo-app/                       # Bundled replaceable full-stack app (HTML + CSS + Java)
|   |-- src/main/java/com/demoapp/  # Spring Boot REST API (tasks CRUD + login)
|   |-- src/main/resources/static/  # index.html, login.html, 404.html, css/style.css, js/app.js
|   |-- src/test/java/              # JUnit 5 tests
|   |-- tests/ postman/ k6/
|   |-- pom.xml Dockerfile docker-compose.yml
|   `-- infrapilot.yml
|-- pipelines/
|   |-- Jenkinsfile
|   |-- shared-library/vars/ runTests, runSecurityScan, runInfracost, runTerraform, notifyStage
|   |-- github-actions/infrapilot.yml
|   `-- gitlab-ci/.gitlab-ci.yml
|-- infrastructure/
|   |-- bootstrap/                  # S3 bucket + DynamoDB lock (run once)
|   |-- modules/ vpc, ec2, iam, ecr
|   `-- templates/single-host/      # main.tf, variables.tf, outputs.tf, user_data.sh
|-- ansible/                        # playbook.yml, inventory, roles/docker, roles/monitoring-agents
|-- monitoring/
|   |-- docker-compose.monitoring.yml
|   |-- prometheus/ prometheus.yml, rules/alerts.yml
|   |-- alertmanager/alertmanager.yml
|   `-- grafana/ provisioning/, dashboards/*.json
|-- security/ gitleaks.toml, trivy.yaml, checkov-baseline.yaml, severity-policy.json
|-- docs/ architecture.md, runbook.md, api-spec.yaml
|-- scripts/ setup-local.sh, seed-dev-data.sql
|-- docker-compose.yml
`-- README.md
```

---

## 4. The `infrapilot.yml` Contract

Place `infrapilot.yml` in the root of any repository to customize its lifecycle:

```yaml
version: 1
app:
  name: demo-app
  type: fullstack # fullstack | backend | frontend
  port: 8080 # port exposed by the main container
  health_path: /actuator/health
  metrics_path: /actuator/prometheus # enables app-level Grafana panels
build:
  command: ./mvnw -B package -DskipTests
  dockerfile: ./Dockerfile
test:
  unit: ./mvnw -B test
  api: newman run tests/postman/collection.json --env-var baseUrl={{APP_URL}}
  load: k6 run tests/k6/smoke.js
deploy:
  target: aws-ec2 # aws-ec2 now; aws-ecs later
  compose_file: docker-compose.yml
  env:
    SPRING_PROFILES_ACTIVE: prod
  secrets:
    - DB_PASSWORD
thresholds:
  max_critical_vulns: 0
  max_high_vulns: 5
  min_coverage_percent: 60
  p95_latency_ms: 500
```

---

## 5. Quick Start (Local Development)

### 1. Initialize Environment
```bash
cp .env.example .env
```

### 2. Start Core Platform Services
```bash
docker compose up -d
```

### 3. Service Dashboard Links
- **InfraPilot Web Dashboard**: [http://localhost:8080](http://localhost:8080)
- **InfraPilot REST API**: [http://localhost:8085](http://localhost:8085)
- **Demo Task Tracker App**: [http://localhost:8081](http://localhost:8081)
- **Grafana Observability**: [http://localhost:3000](http://localhost:3000)
- **Prometheus Scrapes**: [http://localhost:9090](http://localhost:9090)
- **Jenkins Orchestrator**: [http://localhost:8090](http://localhost:8090)
- **SonarQube Quality**: [http://localhost:9000](http://localhost:9000)

---

## 6. MVP Demo Script (12-Step Walkthrough)

1. **Sign in to InfraPilot**: Open `http://localhost:8080`, authenticate with `devops@infrapilot.io` / `password123`.
2. **Click "New Run"**: Choose "Use demo app" with environment `dev`.
3. **Inspect Readiness Report**: Note auto-detected stack (`Java Maven`, port 8080) and contract verification.
4. **Start Run**: Stages dynamically execute: checkout, config load, build, unit tests.
5. **Security Stages Run**: Gitleaks passes, Trivy reports 0 critical, Checkov passes with baseline.
6. **Infracost Completes**: Displays estimated monthly AWS cost ($15.28/mo) against the $25 budget cap.
7. **Terraform Plan Displayed**: Infrastructure changes shown; DevOps user clicks **Approve Apply**.
8. **Apply & Deploy**: Status changes to `Deploying`, then `Deployed` with public endpoint URL.
9. **Smoke & Load Tests**: Newman API tests pass; k6 verifies p95 latency is well below 500ms.
10. **Open Observability Tab**: Live Grafana panels show CPU, memory, disk, container metrics, and request rates.
11. **Stop App Container**: Simulate failure (`docker compose stop demo-app`); Alertmanager fires `AppDown` and health status turns red.
12. **Swap Test Proof**: Input a different GitHub repo URL (e.g., Python Flask or Node.js) and observe automated stack detection and deployment.

---

## 7. Cost Control Rules
- **No NAT Gateways**: The single-host template utilizes a public subnet with security groups to eliminate hourly charges.
- **Instance Rightsizing**: `t3.small` / `t3.micro` with a 20 GB gp3 encrypted volume.
- **AWS Budget Alert**: Keep an active AWS Budget Alert threshold set to $5.00 USD.
- **Always Destroy**: Execute `terraform destroy -auto-approve` or click **Destroy Stack** in `PipelineView` after testing.
