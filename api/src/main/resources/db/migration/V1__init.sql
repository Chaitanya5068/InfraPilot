-- InfraPilot Flyway V1__init.sql (Guide v2.1 Schema)

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'developer', -- admin|devops|developer|viewer
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    repo_url VARCHAR(500) NOT NULL,
    repo_host VARCHAR(10) NOT NULL, -- github|gitlab
    branch VARCHAR(100) DEFAULT 'main',
    detected_stack VARCHAR(30),
    config JSONB, -- parsed infrapilot.yml
    is_demo BOOLEAN DEFAULT false,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    environment VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'queued',
    -- queued|running|awaiting_approval|deploying|deployed|failed|destroyed
    commit_sha VARCHAR(40),
    ci_system VARCHAR(20), -- jenkins|github_actions|gitlab_ci
    ci_ref VARCHAR(200),
    endpoint_url VARCHAR(500),
    grafana_url VARCHAR(500),
    requested_by UUID REFERENCES users(id),
    approved_by UUID REFERENCES users(id),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS stage_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id UUID NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    stage VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL, -- pending|running|passed|failed|warning|skipped
    duration_sec INT,
    log_url VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS scan_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id UUID NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    tool VARCHAR(30) NOT NULL, -- gitleaks|trivy|checkov|sonar|depcheck
    critical INT DEFAULT 0,
    high INT DEFAULT 0,
    medium INT DEFAULT 0,
    low INT DEFAULT 0,
    findings JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS cost_estimates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id UUID NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    monthly_cost NUMERIC(10,2),
    breakdown JSONB,
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS test_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id UUID NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    kind VARCHAR(10) NOT NULL, -- unit|api|load
    total INT,
    failed INT,
    coverage_percent NUMERIC(5,2),
    p95_ms INT,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS audit_events (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    resource_id UUID,
    details JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_runs_project_id ON runs(project_id);
CREATE INDEX IF NOT EXISTS idx_stage_results_run_id ON stage_results(run_id);
CREATE INDEX IF NOT EXISTS idx_scan_results_run_id ON scan_results(run_id);
CREATE INDEX IF NOT EXISTS idx_test_results_run_id ON test_results(run_id);
CREATE INDEX IF NOT EXISTS idx_cost_estimates_run_id ON cost_estimates(run_id);
