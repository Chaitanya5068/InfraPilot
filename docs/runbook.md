# InfraPilot SRE Incident Runbook & Disaster Recovery (DR) Plan

This document details the standard operating procedures (SOPs) for troubleshooting, recovering from production incidents, and executing disaster recovery for the **InfraPilot** platform.

---

## 📋 Table of Contents
1. [Emergency Contacts & Escalation Matrix](#1-emergency-contacts--escalation-matrix)
2. [Incident Severity Levels (P1 - P4)](#2-incident-severity-levels-p1---p4)
3. [Alert Runbooks](#3-alert-runbooks)
   - [Alert 1: HighErrorRateBurn (5xx Spike)](#alert-1-higherrorrateburn-5xx-spike)
   - [Alert 2: ApiLatencyP99Breach (> 250ms)](#alert-2-apilatencyp99breach--250ms)
   - [Alert 3: KubernetesPodCrashLooping](#alert-3-kubernetespodcrashlooping)
   - [Alert 4: DatabasePoolExhaustion (HikariCP Saturation)](#alert-4-databasepoolexhaustion-hikaricp-saturation)
4. [Deployment Rollback Procedure](#4-deployment-rollback-procedure)
5. [Disaster Recovery: PostgreSQL RDS Point-in-Time Restore (PITR)](#5-disaster-recovery-postgresql-rds-point-in-time-restore-pitr)
6. [AWS Systems Manager (SSM) Access (No SSH)](#6-aws-systems-manager-ssm-access-no-ssh)

---

## 1. Emergency Contacts & Escalation Matrix

| Role | Contact | Primary Tool | Escalation SLA |
| :--- | :--- | :--- | :--- |
| **SRE On-Call** | Chaitanya (`sre-oncall@infrapilot.dev`) | PagerDuty / Slack `#sre-alerts` | 5 minutes |
| **DevOps Lead** | Priya Sharma (`priya@infrapilot.dev`) | Slack `#devops-incidents` | 15 minutes |
| **Cloud Security Lead** | Marcus Vance (`security@infrapilot.dev`) | Direct Phone / Slack `#secops` | 15 minutes |

---

## 2. Incident Severity Levels (P1 - P4)

| Level | Definition | Impact | Response Time |
| :--- | :--- | :--- | :--- |
| **P1 - CRITICAL** | Platform Down / Data Corrupted / Active Security Breach | Complete outage for all users | **< 15 mins** |
| **P2 - HIGH** | Core feature degraded (Deployments failing, p99 latency > 1s) | Severe degradation, workaround available | **< 30 mins** |
| **P3 - MEDIUM** | Non-critical component degraded (Metrics lag, slow UI) | Minor degradation | **< 2 hours** |
| **P4 - LOW** | Cosmetic bug, minor log warning | No user impact | **Next sprint** |

---

## 3. Alert Runbooks

### Alert 1: `HighErrorRateBurn` (5xx Spike)
- **PromQL**: `sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m])) * 100 > 1.0`
- **Symptom**: User requests returning HTTP 500 or 502 Bad Gateway.
- **Immediate Steps**:
  1. Open Grafana Golden Signals dashboard & check the HTTP Status breakdown.
  2. Inspect the latest deployment logs:
     ```bash
     kubectl logs -n infrapilot-prod deployment/infrapilot-app --tail=200 | grep -i "ERROR"
     ```
  3. If error coincides with a recent deployment, trigger an **Immediate Rollback** (Section 4).

### Alert 2: `ApiLatencyP99Breach` (> 250ms)
- **PromQL**: `histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m])) > 0.250`
- **Symptom**: Sluggish UI, slow API responses.
- **Immediate Steps**:
  1. Check CPU & Memory utilization on nodes and containers.
  2. Inspect database active connections:
     ```sql
     SELECT pid, age(clock_timestamp(), query_start), usename, query 
     FROM pg_stat_activity 
     WHERE state != 'idle' AND query NOT ILIKE '%pg_stat%' 
     ORDER BY query_start ASC LIMIT 5;
     ```
  3. If slow queries are locking tables, terminate the locking process:
     ```sql
     SELECT pg_terminate_backend(<PID>);
     ```

### Alert 3: `KubernetesPodCrashLooping`
- **PromQL**: `rate(kube_pod_container_status_restarts_total{namespace="infrapilot-prod"}[15m]) * 60 > 2`
- **Symptom**: Container repeatedly crashing upon boot.
- **Immediate Steps**:
  1. Inspect why the container exited:
     ```bash
     kubectl describe pod <pod-name> -n infrapilot-prod
     kubectl logs <pod-name> -n infrapilot-prod --previous
     ```
  2. Common root causes:
     - Missing secret / config variable in `infrapilot-config` ConfigMap.
     - Database unreachable (check security group or RDS health).
     - Out of Memory (OOMKilled) &rarr; Increase container memory limit in `k8s/production-app.yaml`.

### Alert 4: `DatabasePoolExhaustion` (HikariCP Saturation)
- **PromQL**: `(hikaricp_connections_active / hikaricp_connections_max) * 100 > 85`
- **Symptom**: Backend requests queuing or failing with `ConnectionTimeoutException`.
- **Immediate Steps**:
  1. Temporarily increase pool capacity in configuration if database server has available memory.
  2. Identify connection leak: check if transactions are missing `@Transactional` closing or unhandled exceptions.

---

## 4. Deployment Rollback Procedure

When a new version introduces critical defects, perform an immediate zero-downtime rollback:

### Kubernetes Rollback:
```bash
# Check deployment rollout revision history
kubectl rollout history deployment/infrapilot-app -n infrapilot-prod

# Rollback to the previous stable release
kubectl rollout undo deployment/infrapilot-app -n infrapilot-prod

# Verify rollout status
kubectl rollout status deployment/infrapilot-app -n infrapilot-prod
```

### Helm Rollback:
```bash
# View Helm release history
helm history infrapilot -n infrapilot-prod

# Rollback to previous revision (e.g. revision 3)
helm rollback infrapilot 3 -n infrapilot-prod
```

---

## 5. Disaster Recovery: PostgreSQL RDS Point-in-Time Restore (PITR)

In the event of accidental data deletion or database corruption:

1. **Identify Target Recovery Timestamp**:
   Determine the exact UTC time right before the corruption occurred (e.g., `2026-10-25T02:00:00Z`).

2. **Execute Point-in-Time Restore via AWS CLI**:
   ```bash
   aws rds restore-db-instance-to-point-in-time \
     --source-db-instance-identifier infrapilot-postgres-production \
     --target-db-instance-identifier infrapilot-postgres-restored \
     --restore-time 2026-10-25T02:00:00Z \
     --db-subnet-group-name infrapilot-rds-subnet-group \
     --vpc-security-group-ids sg-xxxxxxxxxxxxxxxxx
   ```

3. **Verify Restored Instance**:
   Connect via bastion / private runner to verify data integrity:
   ```bash
   psql -h <restored-rds-endpoint> -U infrapilot_admin -d infrapilot_prod
   ```

4. **Update Secret Pointer**:
   Update `infrapilot/production/db-credentials` in AWS Secrets Manager to point to the new database endpoint. Applications will pick up the new connection automatically.

---

## 6. AWS Systems Manager (SSM) Access (No SSH)

Per production security benchmarks, **SSH port 22 is disabled**. Access EC2 nodes securely via IAM permissions:

```bash
# Start an interactive shell session on an Auto Scaling instance via SSM
aws ssm start-session --target <i-0xxxxxxxxxxxxxxxxx> --region ap-south-1
```
All commands run through SSM are logged in AWS CloudTrail for immutable audit trails.
