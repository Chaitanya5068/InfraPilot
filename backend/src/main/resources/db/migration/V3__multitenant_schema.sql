-- ==============================================================================
-- InfraPilot Database Migration V3: Enterprise Multi-Tenancy & Org Root Admin Isolation
-- File: V3__multitenant_schema.sql
-- Supports: Multiple Companies/Tenants with Isolated Root Admins, RBAC & Row-Level Security
-- ==============================================================================

-- 1. Organizations / Companies Table
CREATE TABLE IF NOT EXISTS organizations (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,          -- e.g. 'infrapilot', 'acme-corp', 'fintech-io'
    domain VARCHAR(100),                       -- e.g. 'infrapilot.dev', 'acme.com', 'fintech.io'
    plan_tier VARCHAR(30) DEFAULT 'ENTERPRISE',-- COMMUNITY, BUSINESS, ENTERPRISE
    logo_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast tenant lookup by slug or domain
CREATE INDEX IF NOT EXISTS idx_organizations_slug ON organizations(slug);
CREATE INDEX IF NOT EXISTS idx_organizations_domain ON organizations(domain);

-- 2. Modify or Link Users Table to Organizations
ALTER TABLE users ADD COLUMN IF NOT EXISTS organization_id VARCHAR(36) REFERENCES organizations(id) ON DELETE CASCADE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_root_admin BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30);
ALTER TABLE users ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN DEFAULT FALSE;

-- Ensure an email is unique per organization (a person can exist in Acme Corp and InfraPilot independently)
DROP INDEX IF EXISTS uk_users_email;
CREATE UNIQUE INDEX IF NOT EXISTS uk_org_user_email ON users(organization_id, email);
CREATE INDEX IF NOT EXISTS idx_users_org ON users(organization_id);

-- 3. Update User Sessions Table with Tenant Isolation
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS organization_id VARCHAR(36) REFERENCES organizations(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_sessions_org ON user_sessions(organization_id);

-- 4. Update Developer Activities Table with Tenant Isolation
ALTER TABLE developer_activities ADD COLUMN IF NOT EXISTS organization_id VARCHAR(36) REFERENCES organizations(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_activities_org ON developer_activities(organization_id);

-- 5. Seed Default Organizations
INSERT INTO organizations (id, name, slug, domain, plan_tier, is_active)
VALUES 
    ('org-infrapilot-core', 'InfraPilot Internal Core', 'infrapilot', 'infrapilot.dev', 'ENTERPRISE', TRUE),
    ('org-acme-corp', 'Acme Cloud Dynamics', 'acme', 'acme.com', 'ENTERPRISE', TRUE),
    ('org-fintech-io', 'FinTech Global Systems', 'fintech', 'fintech.io', 'ENTERPRISE', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 6. Seed Root Super Admins & Teams for Each Organization

-- Organization 1: InfraPilot Core Team
INSERT INTO users (id, organization_id, email, full_name, password_hash, role, is_root_admin, department)
VALUES 
    ('user-chaitanya-01', 'org-infrapilot-core', 'chaitanya@infrapilot.dev', 'Chaitanya', '$2a$12$eXampleHashedPasswordChaitanya', 'PLATFORM_SUPER_ADMIN', TRUE, 'Core Architecture'),
    ('user-priya-01', 'org-infrapilot-core', 'priya@infrapilot.dev', 'Priya Sharma', '$2a$12$eXampleHashedPasswordPriya', 'DEVOPS_LEAD', FALSE, 'Backend Engineering')
ON CONFLICT (id) DO NOTHING;

-- Organization 2: Acme Cloud Dynamics (External Company with their own Root Admin)
INSERT INTO users (id, organization_id, email, full_name, password_hash, role, is_root_admin, department)
VALUES 
    ('user-marcus-acme', 'org-acme-corp', 'marcus.vance@acme.com', 'Marcus Vance', '$2a$12$eXampleHashedPasswordMarcus', 'ORG_ROOT_ADMIN', TRUE, 'Cloud Operations'),
    ('user-elena-acme', 'org-acme-corp', 'elena.rostova@acme.com', 'Elena Rostova', '$2a$12$eXampleHashedPasswordElena', 'LEAD_DEV', FALSE, 'Microservices Squad'),
    ('user-david-acme', 'org-acme-corp', 'david.kim@acme.com', 'David Kim', '$2a$12$eXampleHashedPasswordDavid', 'DEVOPS_ENG', FALSE, 'Infrastructure Team')
ON CONFLICT (id) DO NOTHING;

-- Organization 3: FinTech Global Systems (External Financial Client with Strict Root Admin)
INSERT INTO users (id, organization_id, email, full_name, password_hash, role, is_root_admin, department)
VALUES 
    ('user-sarah-fintech', 'org-fintech-io', 'sarah.jenkins@fintech.io', 'Sarah Jenkins', '$2a$12$eXampleHashedPasswordSarah', 'ORG_ROOT_ADMIN', TRUE, 'InfoSec & Compliance'),
    ('user-arjun-fintech', 'org-fintech-io', 'arjun.patel@fintech.io', 'Arjun Patel', '$2a$12$eXampleHashedPasswordArjun', 'SRE_SPECIALIST', FALSE, 'High-Frequency Payments')
ON CONFLICT (id) DO NOTHING;

-- 7. Row-Level Security (RLS) Setup for Database Isolation
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE developer_activities ENABLE ROW LEVEL SECURITY;

-- Helper policy function:
-- Tenant isolation policy ensures queries only view rows belonging to the active organization session
DROP POLICY IF EXISTS tenant_isolation_users ON users;
CREATE POLICY tenant_isolation_users ON users
    USING (
        organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')
        OR current_setting('app.current_user_role', true) = 'PLATFORM_SUPER_ADMIN'
    );

DROP POLICY IF EXISTS tenant_isolation_sessions ON user_sessions;
CREATE POLICY tenant_isolation_sessions ON user_sessions
    USING (
        organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')
        OR current_setting('app.current_user_role', true) = 'PLATFORM_SUPER_ADMIN'
    );

DROP POLICY IF EXISTS tenant_isolation_activities ON developer_activities;
CREATE POLICY tenant_isolation_activities ON developer_activities
    USING (
        organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')
        OR current_setting('app.current_user_role', true) = 'PLATFORM_SUPER_ADMIN'
    );

-- 8. Organization Audit & Tenant Management View
CREATE OR REPLACE VIEW v_organization_summary AS
SELECT 
    o.id AS org_id,
    o.name AS org_name,
    o.slug AS org_slug,
    o.plan_tier,
    COUNT(DISTINCT u.id) AS total_users,
    COUNT(DISTINCT CASE WHEN u.is_root_admin THEN u.id END) AS total_root_admins,
    COUNT(DISTINCT CASE WHEN s.is_active THEN s.id END) AS active_sessions_count
FROM organizations o
LEFT JOIN users u ON o.id = u.organization_id
LEFT JOIN user_sessions s ON o.id = s.organization_id
GROUP BY o.id, o.name, o.slug, o.plan_tier;
