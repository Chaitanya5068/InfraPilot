-- InfraPilot Development Seed Data Script
-- Populates demo users and sample historical runs for immediate platform evaluation

INSERT INTO users (id, email, password_hash, role)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'admin@infrapilot.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin'),
    ('22222222-2222-2222-2222-222222222222', 'devops@infrapilot.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'devops'),
    ('33333333-3333-3333-3333-333333333333', 'developer@infrapilot.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'developer'),
    ('44444444-4444-4444-4444-444444444444', 'viewer@infrapilot.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'viewer')
ON CONFLICT (email) DO NOTHING;

INSERT INTO projects (id, name, repo_url, repo_host, branch, detected_stack, is_demo, created_by)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'demo-app',
    'https://github.com/infrapilot/demo-app',
    'github',
    'main',
    'Java Maven',
    true,
    '33333333-3333-3333-3333-333333333333'
)
ON CONFLICT (id) DO NOTHING;
