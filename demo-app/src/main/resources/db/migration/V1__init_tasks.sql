-- Demo App Task Tracker Schema (Section 7)

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'TODO', -- TODO, IN_PROGRESS, DONE
    priority VARCHAR(50) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed demo user (password: demo123) and starter tasks
INSERT INTO users (username, password, role)
VALUES ('developer', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO tasks (title, description, status, priority)
VALUES 
    ('Integrate Prometheus & Grafana Monitoring', 'Configure Prometheus scrape targets and dashboards for JVM and host metrics.', 'DONE', 'HIGH'),
    ('Add Automated Security Scanners', 'Run Gitleaks, Trivy, and Checkov in CI/CD pipeline stages.', 'DONE', 'HIGH'),
    ('Provision AWS Single Host Infrastructure', 'Implement Terraform template for VPC, Subnet, Security Groups and EC2 host.', 'IN_PROGRESS', 'MEDIUM'),
    ('Configure Alertmanager Rules', 'Set up alerts for AppDown, HighCPU, and HighMemory.', 'TODO', 'LOW');
