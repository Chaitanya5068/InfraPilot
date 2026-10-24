# InfraPilot

InfraPilot is a multi-tenant internal developer platform for AWS deployments. The project is being built in the phases in the developer guide. Phase 0 provides the local service stack and initial Spring Boot/Vaadin shell; the Terraform AWS foundation is also present from the earlier infrastructure scaffold.

## Stack

- Java 17, Spring Boot 3, Vaadin 24
- PostgreSQL 15, Redis 7
- Terraform 1.6+, AWS provider 5.x
- Docker Compose for local dependencies
- Prometheus and Grafana for local observability

## Repository layout

```text
backend/                    Spring Boot API and Flyway migrations
frontend/                   Vaadin application shell
scripts/                    Local setup and database initialization
infrastructure/
  aws/
    modules/                 Reusable VPC, ALB, EC2, RDS, ECR, and SQS modules
    templates/fullstack-standard/
                            AWS full-stack deployment template
    state-bootstrap/         One-time remote Terraform state resources
monitoring/                  Prometheus configuration and Grafana provisioning
security/                    Scanner policy configuration
```

## Local development services

Install Docker Desktop and Java 17. Install Maven 3.9+, then copy the example environment file and start PostgreSQL, Redis, Vault (development mode), Prometheus, and Grafana:

```powershell
Copy-Item .env.example .env
docker compose up -d
docker compose ps
```

The compose stack creates a non-superuser `infrapilot` database account for the application and Flyway migrations. Service ports are bound to loopback: PostgreSQL `5432`, Redis `6379`, Vault `8200`, Prometheus `9090`, and Grafana `3000`. Vault uses the development token from `.env` and in-memory storage; this configuration is for local development only. Do not expose these services or use this Vault configuration outside a disposable development environment. Grafana's default local credentials are `admin` / `admin`; change them in `.env` before any shared use.

Start the application from the repository root; the script loads local variables from `.env` and launches Maven:

```powershell
.\scripts\run-local.ps1
```

The shell runs at `http://localhost:8080/login`. Check application and database connectivity at `http://localhost:8080/api/v1/health`; successful output includes `"status":"UP"` and `"database":"UP"`. Prometheus is at `http://localhost:9090` and Grafana at `http://localhost:3000`. The Vaadin login form is a Phase 0 UI shell only; authentication is not implemented yet.

Stop services with `docker compose down`. Add `-v` only when intentionally removing local database and cache data.

To create all planned project directories and non-overwriting `.gitkeep` placeholders, run:

```powershell
.\scripts\create-project-structure.ps1
```

## AWS infrastructure

The `fullstack-standard` template builds a VPC with public, private, and database subnets, a public ALB, private EC2 Auto Scaling Group, encrypted PostgreSQL RDS with an AWS-managed master password, ECR, and encrypted SQS queues with dead-letter queues. EC2 has no SSH ingress; use AWS Systems Manager Session Manager. TLS is enabled when an ACM certificate ARN is supplied; without one, the ALB serves HTTP and is suitable only for development.

Before planning:

1. Install Terraform 1.6 or later and configure AWS credentials using the AWS CLI or an approved identity provider.
2. Copy `terraform.tfvars.example` to `terraform.tfvars` and set unique organization, project, deployment, and environment values. Keep `app_image_tag` empty for the first apply; this provisions the registry and supporting infrastructure without starting EC2 instances.
3. For shared state, apply `infrastructure/aws/state-bootstrap` once using an administrator-approved state bucket name. Then initialize the deployment template with the resulting bucket and lock table. Do not use local state for shared or production deployments.
4. Review the plan, including AWS costs, before applying. NAT Gateway, ALB, and RDS incur charges even during the initial apply. No command in this repository applies infrastructure without an explicit operator action.
5. After the initial apply, get the ECR repository URL with `terraform -chdir=infrastructure/aws/templates/fullstack-standard output -raw ecr_repository_url`. Build and push an application image with a unique immutable tag using the AWS CLI and Docker; then set `app_image_tag` to that tag and apply again to launch EC2. The application must listen on `8080` and return HTTP 200-399 at `/actuator/health` (or update the ALB health-check path).

Terraform template commands (after configuring the backend):

```powershell
terraform -chdir=infrastructure/aws/templates/fullstack-standard init `
  -backend-config="bucket=<state-bucket>" `
  -backend-config="key=<org>/<project>/<environment>/terraform.tfstate" `
  -backend-config="region=ap-south-1" `
  -backend-config="dynamodb_table=<lock-table>" `
  -backend-config="encrypt=true"
terraform -chdir=infrastructure/aws/templates/fullstack-standard plan -var-file=terraform.tfvars
```

Keep backend configuration values in your deployment environment; never put credentials in Terraform files. The bootstrap stack uses a separately managed state location and must not be pointed at the bucket it creates.

## Build phases

- Phase 0: local services, initial Flyway schema, health endpoint, and Vaadin shell.
- Phase 1: AWS infrastructure modules and template are in place; validate in a non-production AWS account before use.
- Later phases: Jenkins pipeline and shared library, application/API and SQS orchestration, Vaadin dashboard, tenant RLS/security controls, then production observability and operational runbooks.

The PDF guide is the product specification. In particular, its production requirements include PostgreSQL row-level security, tenant-scoped authorization, immutable audit events, secret management, approval gates, and security scans. Phase 0 configures initial row-level security policies, but transaction-scoped tenant context and tenant authorization will be completed in Phase 3. Infrastructure modules alone do not implement or certify these application-level controls.
