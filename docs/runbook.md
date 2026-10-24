# InfraPilot Operations Runbook & Troubleshooting Guide

## 1. Quick Operations Reference

### Starting the Local Development Stack
```bash
# 1. Prepare environment variables
cp .env.example .env

# 2. Start PostgreSQL, Demo App, Prometheus, Grafana, Jenkins, and Exporters
docker compose up -d

# 3. Verify health status of all targets
docker compose ps
curl -i http://localhost:8085/api/v1/health
```

### Access URLs
- **InfraPilot Dashboard**: `http://localhost:8080`
- **InfraPilot API**: `http://localhost:8085`
- **Demo Task Tracker App**: `http://localhost:8081`
- **Grafana Observability**: `http://localhost:3000` (admin/admin or anonymous)
- **Prometheus Dashboard**: `http://localhost:9090`
- **Jenkins Orchestrator**: `http://localhost:8090`
- **SonarQube Quality**: `http://localhost:9000`

---

## 2. Real-World Engineering Problems Solved

### Issue 1: Grafana Iframe Embedding Blocked
- **Symptoms**: The Vaadin `MonitorView` iframe rendered blank or displayed an `X-Frame-Options: DENY` browser console error.
- **Root Cause**: Grafana defaults to strict clickjacking prevention, disabling cross-origin frame embedding.
- **Resolution**: Configured `GF_SECURITY_ALLOW_EMBEDDING=true` and enabled anonymous viewer access (`GF_AUTH_ANONYMOUS_ENABLED=true`) in Grafana configuration and Docker Compose environment.

### Issue 2: Terraform State Locking Deadlock
- **Symptoms**: Pipeline runs failed during `terraform plan` with `Error: Error acquiring the state lock: ConditionalCheckFailedException`.
- **Root Cause**: A previous CI run was aborted midway, leaving an orphaned state lock entry in DynamoDB (`infrapilot-tfstate-lock`).
- **Resolution**: Implemented automatic `terraform force-unlock <LOCK_ID>` after checking if any active worker process is alive, and added pipeline cleanup traps in `Jenkinsfile`.

### Issue 3: Infracost FinOps Cost Spikes on Unattended Demo Runs
- **Symptoms**: Cloud bill risks from idle EC2 instances and attached public IPs.
- **Root Cause**: Developers forgetting to destroy demo environments after recording presentations.
- **Resolution**:
  1. Configured an AWS Budget Alert at $5.00 USD with instant email/webhook notification.
  2. Implemented single-host public subnet template avoiding costly $35/month NAT Gateways.
  3. Added an explicit `POST /api/v1/runs/{id}/destroy` endpoint and UI button in `PipelineView`.

### Issue 4: SSRF & Remote Code Execution Prevention on Arbitrary Repos
- **Symptoms**: Onboarding untrusted Git repository URLs posed SSRF and command injection vulnerabilities.
- **Root Cause**: Passing user-supplied URLs directly into shell strings (e.g. `sh "git clone $URL"`).
- **Resolution**:
  1. Enforced strict URL prefix validation (`https://github.com/` or `https://gitlab.com/` only).
  2. Replaced shell strings with Java `ProcessBuilder` utilizing distinct argv array tokens.
  3. Executed all builds inside disposable Docker containers without AWS credential mounts.
