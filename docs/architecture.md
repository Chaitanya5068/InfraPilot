# InfraPilot Architecture & Engineering Specification

## 1. Overview
InfraPilot is a post-development platform that allows engineers to paste a Git repository link and immediately receive an automated, enterprise-grade deployment lifecycle: CI/CD orchestration, static code quality analysis, shift-left multi-layer security scanning, cloud FinOps cost projection, Infrastructure as Code provisioning, and full-stack observability.

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

## 2. Core Architectural Layers

| Layer | Component | Responsibility |
|---|---|---|
| **UI** | Vaadin 24 Views | Responsive control plane: repo submission, live pipeline status (`@Push`), security card metrics, cost breakdowns, and embedded Grafana panels |
| **API** | Spring Boot 3 + Spring Security (JWT) | REST API endpoints for user authentication, project tracking, runs, DevOps approval gates, and CI callback ingestion |
| **Orchestrator** | `RunService` + `JenkinsClient` / `GitHubClient` / `GitLabClient` | Triggers the appropriate CI runtime based on repository contract |
| **CI/CD** | Jenkins shared library, GitHub Actions, GitLab CI | Executes build, test, and scanning stages; reports metrics back to API callbacks |
| **IaC** | Terraform (AWS Provider ~> 5.0) | Declarative AWS topology: VPC, public subnets, route tables, least-privilege security groups, and hardened EC2 instances with IMDSv2 |
| **Config** | Ansible / `user_data.sh` | Idempotent host preparation, Docker runtime setup, and containerized deployment |
| **Data** | PostgreSQL 15 + Flyway | Relational persistence of users, projects, runs, stage results, security scan findings, and audit trails |
| **Monitoring** | Prometheus, Grafana 10, Alertmanager, cAdvisor, Node Exporter | Full-stack observability: RED metrics, JVM runtime metrics, host telemetry, and SRE alerts |

## 3. The `infrapilot.yml` Replace-Me Contract
Any web application containing an `infrapilot.yml` in its root directory can be run through the platform. When absent, the `RepoAnalyzer` auto-detects the tech stack (Maven, Gradle, Node, Python, Dockerfile) and proposes a template.
