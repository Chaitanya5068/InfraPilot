-- InfraPilot Flyway V2__results.sql (Seed Data for Quick Start & Demo Flow)

-- Seed default users with BCrypt hash for 'password123'
-- ($2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
INSERT INTO users (id, email, password_hash, role)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'admin@infrapilot.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin'),
    ('22222222-2222-2222-2222-222222222222', 'devops@infrapilot.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'devops'),
    ('33333333-3333-3333-3333-333333333333', 'developer@infrapilot.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'developer'),
    ('44444444-4444-4444-4444-444444444444', 'viewer@infrapilot.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'viewer')
ON CONFLICT (email) DO NOTHING;

-- Seed default Demo App Project
INSERT INTO projects (id, name, repo_url, repo_host, branch, detected_stack, config, is_demo, created_by)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'demo-app',
    'https://github.com/infrapilot/demo-app',
    'github',
    'main',
    'Java Maven',
    '{"version": 1, "app": {"name": "demo-app", "type": "fullstack", "port": 8080, "health_path": "/actuator/health", "metrics_path": "/actuator/prometheus"}, "build": {"command": "./mvnw -B package -DskipTests", "dockerfile": "./Dockerfile"}, "test": {"unit": "./mvnw -B test", "api": "newman run tests/postman/collection.json --env-var baseUrl={{APP_URL}}", "load": "k6 run tests/k6/smoke.js"}, "deploy": {"target": "aws-ec2", "compose_file": "docker-compose.yml", "env": {"SPRING_PROFILES_ACTIVE": "prod"}}, "thresholds": {"max_critical_vulns": 0, "max_high_vulns": 5, "min_coverage_percent": 60, "p95_latency_ms": 500}}'::jsonb,
    true,
    '33333333-3333-3333-3333-333333333333'
)
ON CONFLICT (id) DO NOTHING;

-- Seed a sample historical successful run for immediate visualization
INSERT INTO runs (id, project_id, environment, status, commit_sha, ci_system, ci_ref, endpoint_url, grafana_url, requested_by, approved_by, started_at, finished_at)
VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'dev',
    'deployed',
    'd8a9f23',
    'jenkins',
    'build-42',
    'http://localhost:8081',
    'http://localhost:3000',
    '33333333-3333-3333-3333-333333333333',
    '22222222-2222-2222-2222-222222222222',
    now() - interval '1 hour',
    now() - interval '45 minutes'
)
ON CONFLICT (id) DO NOTHING;

-- Seed stage results for historical run
INSERT INTO stage_results (run_id, stage, status, duration_sec, log_url)
VALUES 
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'checkout', 'passed', 4, '/logs/checkout.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'build', 'passed', 42, '/logs/build.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'unit_tests', 'passed', 18, '/logs/unit_tests.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'sonar', 'passed', 35, '/logs/sonar.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'secret_scan', 'passed', 6, '/logs/secret_scan.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'dependency_scan', 'passed', 28, '/logs/dependency_scan.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'iac_scan', 'passed', 14, '/logs/iac_scan.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'image_build', 'passed', 52, '/logs/image_build.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'image_scan', 'passed', 24, '/logs/image_scan.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'infracost', 'passed', 9, '/logs/infracost.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'terraform_plan', 'passed', 15, '/logs/terraform_plan.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'approval', 'passed', 5, '/logs/approval.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'terraform_apply', 'passed', 80, '/logs/terraform_apply.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'deploy', 'passed', 32, '/logs/deploy.log'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'smoke_load_test', 'passed', 36, '/logs/smoke_load_test.log');

-- Seed scan results
INSERT INTO scan_results (run_id, tool, critical, high, medium, low, findings)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'gitleaks', 0, 0, 0, 0, '[]'::jsonb),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'trivy', 0, 0, 1, 3, '[{"id": "CVE-2023-4586", "severity": "MEDIUM", "package": "netty", "title": "Netty HTTP client pipeline issue"}]'::jsonb),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'checkov', 0, 0, 2, 1, '[{"check_id": "CKV_AWS_135", "severity": "MEDIUM", "name": "Ensure EBS volume is encrypted"}]'::jsonb),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'sonar', 0, 0, 0, 4, '[{"rule": "squid:S1186", "severity": "INFO", "message": "Add a nested comment explaining why this method is empty"}]'::jsonb);

-- Seed cost estimates
INSERT INTO cost_estimates (run_id, monthly_cost, breakdown, currency)
VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    15.28,
    '{"totalMonthlyCost": "15.28", "resources": [{"name": "aws_instance.host", "monthlyCost": "15.28", "costComponents": [{"name": "Compute (t3.small)", "monthlyCost": "15.28"}]}]}'::jsonb,
    'USD'
);

-- Seed test results
INSERT INTO test_results (run_id, kind, total, failed, coverage_percent, p95_ms)
VALUES 
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'unit', 12, 0, 84.50, NULL),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'api', 6, 0, NULL, 65),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'load', 600, 0, NULL, 120);

-- Seed audit event
INSERT INTO audit_events (user_id, action, resource, resource_id, details)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    'RUN_CREATED',
    'RUN',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '{"environment": "dev", "mode": "demo"}'::jsonb
);
