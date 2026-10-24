-- ==============================================================================
-- InfraPilot Migration V2: Authentication, Sessions & Activity Monitoring
-- ==============================================================================

-- 1. USER SESSIONS TABLE (For JWT / Refresh Tokens & Session Revocation)
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token_hash VARCHAR(64) NOT NULL UNIQUE,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    last_active_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token_hash ON user_sessions(session_token_hash);
CREATE INDEX idx_user_sessions_expires ON user_sessions(expires_at) WHERE is_revoked = FALSE;

-- 2. LOGIN AUDIT & HISTORY TABLE (Tracks all successful and blocked logins)
CREATE TABLE IF NOT EXISTS login_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    attempted_email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    login_status VARCHAR(20) NOT NULL CHECK (login_status IN ('SUCCESS', 'FAILED_PASSWORD', 'MFA_REQUIRED', 'LOCKED', 'BLOCKED_IP')),
    failure_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_login_history_user_id ON login_history(user_id);
CREATE INDEX idx_login_history_created_at ON login_history(created_at DESC);
CREATE INDEX idx_login_history_ip ON login_history(ip_address, created_at DESC);

-- 3. DEVELOPER ACTIVITY MONITORING TABLE (Live feed of team actions)
CREATE TABLE IF NOT EXISTS developer_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    actor_name VARCHAR(100) NOT NULL,
    actor_avatar VARCHAR(10) NOT NULL,
    action_type VARCHAR(30) NOT NULL CHECK (action_type IN ('COMMIT', 'DEPLOY', 'REVIEW', 'MERGE', 'ALERT', 'HOTFIX', 'CONFIG_CHANGE')),
    target_resource VARCHAR(255) NOT NULL,
    repository_name VARCHAR(150),
    branch_name VARCHAR(100),
    commit_sha VARCHAR(40),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_dev_activities_org_created ON developer_activities(org_id, created_at DESC);
CREATE INDEX idx_dev_activities_user_id ON developer_activities(user_id);
CREATE INDEX idx_dev_activities_repo ON developer_activities(repository_name);

-- 4. DEVSECOPS SECURITY SCAN RESULTS TABLE (Gitleaks, Checkov, Trivy, Semgrep)
CREATE TABLE IF NOT EXISTS repository_security_scans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    scanned_by UUID REFERENCES users(id) ON DELETE SET NULL,
    repository_url VARCHAR(255) NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    commit_sha VARCHAR(40),
    security_score INT NOT NULL CHECK (security_score BETWEEN 0 AND 100),
    grade VARCHAR(5) NOT NULL,
    critical_count INT NOT NULL DEFAULT 0,
    high_count INT NOT NULL DEFAULT 0,
    medium_count INT NOT NULL DEFAULT 0,
    passed_count INT NOT NULL DEFAULT 0,
    findings_data JSONB NOT NULL DEFAULT '[]'::jsonb,
    sarif_report JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_security_scans_repo ON repository_security_scans(repository_url, created_at DESC);

-- 5. ROW-LEVEL SECURITY (RLS) FOR MULTI-TENANT ISOLATION
ALTER TABLE developer_activities ENABLE ROW LEVEL SECURITY;
ALTER TABLE repository_security_scans ENABLE ROW LEVEL SECURITY;

CREATE POLICY dev_activities_tenant ON developer_activities
    USING (org_id = current_setting('app.org_id')::UUID)
    WITH CHECK (org_id = current_setting('app.org_id')::UUID);

CREATE POLICY security_scans_tenant ON repository_security_scans
    USING (org_id = current_setting('app.org_id')::UUID)
    WITH CHECK (org_id = current_setting('app.org_id')::UUID);
