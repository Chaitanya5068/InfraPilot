// InfraPilot Application State & Interactive Engine
const state = {
  currentView: 'dashboard',
  backendConnected: false,
  deployments: [
    {
      id: "dep-a19e2f",
      project: "ecommerce-core",
      env: "dev",
      provider: "AWS (ap-south-1)",
      template: "fullstack-standard",
      status: "deployed",
      cost: "$23.40/mo",
      commit: "b8c3d9a",
      timestamp: "10 mins ago",
      endpoint: "http://alb-infrapilot-dev.ap-south-1.elb.amazonaws.com"
    },
    {
      id: "dep-7c81d2",
      project: "payment-gateway",
      env: "staging",
      provider: "AWS (ap-south-1)",
      template: "container-standard",
      status: "deployed",
      cost: "$48.80/mo",
      commit: "e1a903f",
      timestamp: "1 hour ago",
      endpoint: "http://alb-payments-staging.ap-south-1.elb.amazonaws.com"
    },
    {
      id: "dep-90bc13",
      project: "analytics-worker",
      env: "prod",
      provider: "AWS (ap-south-1)",
      template: "fullstack-standard",
      status: "awaiting_approval",
      cost: "$124.50/mo",
      commit: "f7612c0",
      timestamp: "Just now",
      endpoint: "Pending Approval"
    }
  ],
  securityFindings: [
    { tool: "Gitleaks", type: "Secret Scan", severity: "PASSED", details: "0 secrets detected in repository commit", time: "10m ago" },
    { tool: "Checkov", type: "IaC Security", severity: "PASSED", details: "VPC, RDS, and EC2 comply with CIS AWS Benchmark", time: "10m ago" },
    { tool: "Checkov", type: "IaC Security", severity: "MEDIUM", details: "CKV_AWS_135: Ensure EBS volume encryption by default is enabled", time: "1h ago" },
    { tool: "Trivy", type: "Container CVE", severity: "PASSED", details: "0 CRITICAL vulnerabilities in application base image", time: "10m ago" }
  ],
  auditEvents: [
    { timestamp: "2026-10-24 20:30:15", user: "chaitanya (admin)", action: "DEPLOYMENT_TRIGGERED", resource: "ecommerce-core", ip: "127.0.0.1" },
    { timestamp: "2026-10-24 20:30:42", user: "system (gitleaks)", action: "SECURITY_SCAN_PASSED", resource: "dep-a19e2f", ip: "10.0.1.4" },
    { timestamp: "2026-10-24 20:31:10", user: "chaitanya (devops)", action: "APPROVAL_GRANTED", resource: "dep-a19e2f", ip: "127.0.0.1" },
    { timestamp: "2026-10-24 20:32:05", user: "system (terraform)", action: "INFRA_PROVISIONED", resource: "aws_vpc.main", ip: "10.0.2.18" }
  ],
  pipelineRunning: false
};

// Developer activity data for the right-side panel
const teamActivities = [
  {
    name: "Priya",
    avatar: "P",
    color: "linear-gradient(135deg, #6366F1, #8B5CF6)",
    action: "pushed 3 commits to",
    target: "payment-gateway/feature-auth",
    type: "commit",
    time: "2m ago"
  },
  {
    name: "Arjun",
    avatar: "A",
    color: "linear-gradient(135deg, #3B82F6, #06B6D4)",
    action: "triggered deploy for",
    target: "analytics-worker (staging)",
    type: "deploy",
    time: "5m ago"
  },
  {
    name: "Sneha",
    avatar: "S",
    color: "linear-gradient(135deg, #F59E0B, #EF4444)",
    action: "approved PR #142 on",
    target: "ecommerce-core/main",
    type: "review",
    time: "8m ago"
  },
  {
    name: "Priya",
    avatar: "P",
    color: "linear-gradient(135deg, #6366F1, #8B5CF6)",
    action: "merged branch",
    target: "hotfix/db-connection-pool",
    type: "merge",
    time: "12m ago"
  },
  {
    name: "Arjun",
    avatar: "A",
    color: "linear-gradient(135deg, #3B82F6, #06B6D4)",
    action: "ran Checkov scan on",
    target: "infra/modules/rds",
    type: "review",
    time: "18m ago"
  },
  {
    name: "Sneha",
    avatar: "S",
    color: "linear-gradient(135deg, #F59E0B, #EF4444)",
    action: "opened issue",
    target: "UI: dashboard refresh lag #287",
    type: "alert",
    time: "25m ago"
  },
  {
    name: "Ravi",
    avatar: "R",
    color: "linear-gradient(135deg, #6B7280, #9CA3AF)",
    action: "updated SLO config for",
    target: "monitoring/prometheus-rules",
    type: "commit",
    time: "32m ago"
  },
  {
    name: "Priya",
    avatar: "P",
    color: "linear-gradient(135deg, #6366F1, #8B5CF6)",
    action: "resolved alert on",
    target: "RDS CPU spike — auto-scaled",
    type: "alert",
    time: "40m ago"
  },
  {
    name: "Arjun",
    avatar: "A",
    color: "linear-gradient(135deg, #3B82F6, #06B6D4)",
    action: "pushed Terraform changes to",
    target: "infra/modules/vpc-peering",
    type: "commit",
    time: "48m ago"
  },
  {
    name: "Sneha",
    avatar: "S",
    color: "linear-gradient(135deg, #F59E0B, #EF4444)",
    action: "deployed hotfix to",
    target: "ecommerce-core (dev)",
    type: "deploy",
    time: "55m ago"
  }
];

// Initialize app on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  setupNavigation();
  setupDeploymentWizard();
  renderDeployments();
  renderSecurityTable();
  renderAuditTable();
  renderActivityFeed();
  renderCommitHeatmap();
  renderSparklines();
  checkBackendHealth();
  startLiveActivitySimulation();
});

// View Navigation
function setupNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const targetView = item.getAttribute('data-view');
      switchView(targetView);
    });
  });
}

function switchView(viewName) {
  state.currentView = viewName;
  
  // Update sidebar active class
  document.querySelectorAll('.nav-item').forEach(el => {
    el.classList.toggle('active', el.getAttribute('data-view') === viewName);
  });
  
  // Show target section
  document.querySelectorAll('.view-section').forEach(sec => {
    sec.classList.remove('active-view');
  });
  const activeSection = document.getElementById(`view-${viewName}`);
  if (activeSection) {
    activeSection.classList.add('active-view');
  }
}

// Render Deployments Table
function renderDeployments() {
  const tbody = document.getElementById('deployments-tbody');
  if (!tbody) return;

  tbody.innerHTML = state.deployments.map(dep => {
    let badgeClass = 'badge-success';
    if (dep.status === 'awaiting_approval') badgeClass = 'badge-warning';
    if (dep.status === 'running') badgeClass = 'badge-info';
    if (dep.status === 'failed') badgeClass = 'badge-danger';

    return `
      <tr>
        <td><strong>${dep.project}</strong><br><small style="color: var(--text-dim)">${dep.id}</small></td>
        <td><span class="badge ${badgeClass}">${dep.status.replace('_', ' ')}</span></td>
        <td><code>${dep.env}</code></td>
        <td>${dep.provider}</td>
        <td>${dep.cost}</td>
        <td><a href="${dep.endpoint}" target="_blank" style="color: var(--accent-cyan); text-decoration: none;">${dep.endpoint.startsWith('http') ? 'Launch App ↗' : dep.endpoint}</a></td>
        <td>
          <button class="btn btn-secondary" style="padding: 4px 10px; font-size: 0.8rem;" onclick="viewPipelineDetails('${dep.id}')">View</button>
        </td>
      </tr>
    `;
  }).join('');
}

// Render Security Table
function renderSecurityTable() {
  const tbody = document.getElementById('security-tbody');
  if (!tbody) return;

  tbody.innerHTML = state.securityFindings.map(find => {
    let badgeClass = 'badge-success';
    if (find.severity === 'CRITICAL') badgeClass = 'badge-danger';
    if (find.severity === 'HIGH' || find.severity === 'MEDIUM') badgeClass = 'badge-warning';

    return `
      <tr>
        <td><strong>${find.tool}</strong></td>
        <td>${find.type}</td>
        <td><span class="badge ${badgeClass}">${find.severity}</span></td>
        <td>${find.details}</td>
        <td>${find.time}</td>
      </tr>
    `;
  }).join('');
}

// Render Audit Table
function renderAuditTable() {
  const tbody = document.getElementById('audit-tbody');
  if (!tbody) return;

  tbody.innerHTML = state.auditEvents.map(evt => `
    <tr>
      <td><code>${evt.timestamp}</code></td>
      <td><strong>${evt.user}</strong></td>
      <td><span class="badge badge-neutral">${evt.action}</span></td>
      <td><code>${evt.resource}</code></td>
      <td>${evt.ip}</td>
    </tr>
  `).join('');
}

// ===== DEVELOPER ACTIVITY PANEL =====

function renderActivityFeed() {
  const feed = document.getElementById('activity-feed');
  if (!feed) return;

  const dotTypeMap = {
    commit: 'ai-dot--commit',
    deploy: 'ai-dot--deploy',
    review: 'ai-dot--review',
    merge: 'ai-dot--merge',
    alert: 'ai-dot--alert'
  };

  feed.innerHTML = teamActivities.map(act => `
    <div class="activity-item">
      <div class="ai-icon" style="background: ${act.color};">${act.avatar}</div>
      <div class="ai-body">
        <div class="ai-text">
          <span class="ai-dot ${dotTypeMap[act.type] || ''}"></span>
          <strong>${act.name}</strong> ${act.action} <strong>${act.target}</strong>
        </div>
        <div class="ai-time">${act.time}</div>
      </div>
    </div>
  `).join('');
}

function renderCommitHeatmap() {
  const container = document.getElementById('commit-heatmap');
  if (!container) return;

  // Generate 7x7 grid of 49 cells with random heat levels
  const heatLevels = [0, 0, 0, 1, 1, 1, 2, 2, 3, 3, 4];
  let html = '';
  for (let i = 0; i < 49; i++) {
    const level = heatLevels[Math.floor(Math.random() * heatLevels.length)];
    const cls = level > 0 ? ` heat-${level}` : '';
    html += `<div class="heatmap-cell${cls}" title="Commits: ${level}"></div>`;
  }
  container.innerHTML = html;
}

// Mini sparkline charts on stat cards
function renderSparklines() {
  const sparkConfigs = [
    { id: 'spark-deployments', data: [2, 3, 2, 4, 3, 5, 3], color: '#818CF8' },
    { id: 'spark-dora', data: [6.1, 5.8, 5.2, 4.9, 4.5, 4.3, 4.2], color: '#38BDF8' },
    { id: 'spark-security', data: [100, 100, 98, 100, 100, 100, 100], color: '#34D399' },
    { id: 'spark-cost', data: [145, 152, 168, 174, 180, 189, 196.7], color: '#FBBF24' }
  ];

  sparkConfigs.forEach(cfg => {
    const container = document.getElementById(cfg.id);
    if (!container) return;

    const canvas = document.createElement('canvas');
    canvas.width = 144;
    canvas.height = 56;
    container.appendChild(canvas);

    const ctx = canvas.getContext('2d');
    const data = cfg.data;
    const max = Math.max(...data);
    const min = Math.min(...data);
    const range = max - min || 1;
    const w = canvas.width;
    const h = canvas.height;
    const padding = 4;

    ctx.beginPath();
    data.forEach((val, i) => {
      const x = padding + (i / (data.length - 1)) * (w - padding * 2);
      const y = h - padding - ((val - min) / range) * (h - padding * 2);
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.strokeStyle = cfg.color;
    ctx.lineWidth = 2;
    ctx.lineJoin = 'round';
    ctx.lineCap = 'round';
    ctx.stroke();

    // Fill gradient under the line
    const lastX = padding + (w - padding * 2);
    ctx.lineTo(lastX, h);
    ctx.lineTo(padding, h);
    ctx.closePath();
    const grad = ctx.createLinearGradient(0, 0, 0, h);
    grad.addColorStop(0, cfg.color + '30');
    grad.addColorStop(1, cfg.color + '00');
    ctx.fillStyle = grad;
    ctx.fill();
  });
}

// Simulate live activity updates
function startLiveActivitySimulation() {
  const liveActions = [
    { name: "Priya", avatar: "P", color: "linear-gradient(135deg, #6366F1, #8B5CF6)", action: "pushed to", target: "feature/api-v2", type: "commit" },
    { name: "Arjun", avatar: "A", color: "linear-gradient(135deg, #3B82F6, #06B6D4)", action: "scaled up replicas for", target: "payment-gateway (prod)", type: "deploy" },
    { name: "Sneha", avatar: "S", color: "linear-gradient(135deg, #F59E0B, #EF4444)", action: "commented on PR #156 in", target: "ecommerce-core", type: "review" },
    { name: "Ravi", avatar: "R", color: "linear-gradient(135deg, #6B7280, #9CA3AF)", action: "acknowledged alert for", target: "high CPU on EC2 i-0a3f", type: "alert" },
    { name: "Priya", avatar: "P", color: "linear-gradient(135deg, #6366F1, #8B5CF6)", action: "merged PR #143 into", target: "main branch", type: "merge" },
    { name: "Arjun", avatar: "A", color: "linear-gradient(135deg, #3B82F6, #06B6D4)", action: "updated Terraform module", target: "infra/modules/secrets-mgr", type: "commit" },
    { name: "Sneha", avatar: "S", color: "linear-gradient(135deg, #F59E0B, #EF4444)", action: "ran smoke tests on", target: "analytics-worker (dev)", type: "deploy" },
  ];

  let idx = 0;

  setInterval(() => {
    const feed = document.getElementById('activity-feed');
    if (!feed || state.currentView !== 'dashboard') return;

    const act = liveActions[idx % liveActions.length];
    idx++;

    const dotTypeMap = {
      commit: 'ai-dot--commit',
      deploy: 'ai-dot--deploy',
      review: 'ai-dot--review',
      merge: 'ai-dot--merge',
      alert: 'ai-dot--alert'
    };

    const item = document.createElement('div');
    item.className = 'activity-item';
    item.innerHTML = `
      <div class="ai-icon" style="background: ${act.color};">${act.avatar}</div>
      <div class="ai-body">
        <div class="ai-text">
          <span class="ai-dot ${dotTypeMap[act.type] || ''}"></span>
          <strong>${act.name}</strong> ${act.action} <strong>${act.target}</strong>
        </div>
        <div class="ai-time">just now</div>
      </div>
    `;

    feed.insertBefore(item, feed.firstChild);

    // Keep feed trimmed to 15 items
    while (feed.children.length > 15) {
      feed.removeChild(feed.lastChild);
    }
  }, 8000);
}

// Setup Deployment Wizard
function setupDeploymentWizard() {
  const form = document.getElementById('deploy-form');
  const templateSelect = document.getElementById('deploy-template');
  const envSelect = document.getElementById('deploy-env');
  const costEstimateDisplay = document.getElementById('cost-estimate-preview');

  function updateCostPreview() {
    const env = envSelect.value;
    const template = templateSelect.value;
    let base = 23.40;
    if (template === 'container-standard') base = 48.80;
    if (env === 'staging') base *= 2;
    if (env === 'prod') base *= 4;
    costEstimateDisplay.textContent = `$${base.toFixed(2)} / month`;
  }

  if (templateSelect && envSelect) {
    templateSelect.addEventListener('change', updateCostPreview);
    envSelect.addEventListener('change', updateCostPreview);
  }

  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      
      const projectName = document.getElementById('deploy-project').value || 'new-service';
      const env = envSelect.value;
      const provider = document.getElementById('deploy-provider').value;
      const template = templateSelect.value;
      const cost = costEstimateDisplay.textContent;

      const newDep = {
        id: `dep-${Math.random().toString(36).substr(2, 6)}`,
        project: projectName,
        env: env,
        provider: `${provider} (ap-south-1)`,
        template: template,
        status: 'running',
        cost: cost,
        commit: 'a14f7b2',
        timestamp: 'Just now',
        endpoint: 'Deploying...'
      };

      state.deployments.unshift(newDep);
      renderDeployments();

      // Log audit
      state.auditEvents.unshift({
        timestamp: new Date().toISOString().replace('T', ' ').substr(0, 19),
        user: "chaitanya (devops)",
        action: "DEPLOYMENT_SUBMITTED",
        resource: newDep.id,
        ip: "127.0.0.1"
      });
      renderAuditTable();

      // Switch to pipeline view and run simulation
      switchView('pipelines');
      runPipelineSimulation(newDep);
    });
  }
}

// Real-Time Pipeline Runner Simulation
async function runPipelineSimulation(dep) {
  if (state.pipelineRunning) return;
  state.pipelineRunning = true;

  const terminal = document.getElementById('pipeline-terminal');
  const approvalModal = document.getElementById('approval-modal');
  terminal.innerHTML = '';

  function log(msg, type = 'info') {
    const time = new Date().toLocaleTimeString();
    const line = document.createElement('div');
    line.className = 'log-line';
    line.innerHTML = `<span class="log-time">[${time}]</span> <span class="log-${type}">${msg}</span>`;
    terminal.appendChild(line);
    terminal.scrollTop = terminal.scrollHeight;
  }

  const steps = [
    { id: 'step-1', name: 'Checkout', delay: 1200, logMsg: 'Cloning repository from GitHub branch main... Commit: ' + dep.commit },
    { id: 'step-2', name: 'Unit Tests', delay: 1400, logMsg: 'Running Maven JUnit 5 test suite... 24 tests passed.' },
    { id: 'step-3', name: 'Gitleaks Scan', delay: 1200, logMsg: 'Scanning for leaked secrets and AWS tokens... [PASSED] 0 secrets found.' },
    { id: 'step-4', name: 'Checkov IaC', delay: 1300, logMsg: 'Validating Terraform AWS security policies... [PASSED] 14 checks compliant.' },
    { id: 'step-5', name: 'Infracost', delay: 1100, logMsg: `Infracost breakdown: Monthly cost calculated at ${dep.cost}` },
    { id: 'step-6', name: 'Terraform Plan', delay: 1500, logMsg: 'Generating Terraform execution plan: 12 resources to add (VPC, ALB, EC2, RDS).' },
    { id: 'step-7', name: 'Approval Gate', delay: 0, isApproval: true, logMsg: 'STAGING/PROD Gate: Awaiting DevOps team approval before apply...' },
    { id: 'step-8', name: 'Terraform Apply', delay: 2500, logMsg: 'Applying Terraform plan... Provisioning AWS VPC, Subnets, RDS, and ALB target groups.' },
    { id: 'step-9', name: 'Smoke Tests', delay: 1200, logMsg: 'Running smoke tests against ALB health check endpoint /actuator/health... Status: 200 OK' }
  ];

  // Reset stepper
  document.querySelectorAll('.step-item').forEach(s => s.classList.remove('active', 'completed'));

  log(`🚀 Starting InfraPilot automated deployment: ${dep.id} (${dep.project})`, 'info');

  for (let i = 0; i < steps.length; i++) {
    const step = steps[i];
    const stepEl = document.getElementById(step.id);
    if (stepEl) {
      document.querySelectorAll('.step-item').forEach(s => s.classList.remove('active'));
      stepEl.classList.add('active');
    }

    log(step.logMsg, 'info');

    if (step.isApproval) {
      // Pause at Approval Gate
      dep.status = 'awaiting_approval';
      renderDeployments();
      
      const approved = await waitForApproval(approvalModal, dep);
      if (!approved) {
        log('❌ Deployment was REJECTED by operator. Pipeline aborted.', 'error');
        dep.status = 'failed';
        renderDeployments();
        state.pipelineRunning = false;
        return;
      }
      log('✅ Approval confirmed by user. Proceeding to Terraform Apply.', 'success');
      state.auditEvents.unshift({
        timestamp: new Date().toISOString().replace('T', ' ').substr(0, 19),
        user: "chaitanya (approver)",
        action: "APPROVAL_GRANTED",
        resource: dep.id,
        ip: "127.0.0.1"
      });
      renderAuditTable();
    } else {
      await new Promise(r => setTimeout(r, step.delay));
    }

    if (stepEl) {
      stepEl.classList.remove('active');
      stepEl.classList.add('completed');
    }
  }

  // Completion
  log(`🎉 DEPLOYMENT SUCCESSFUL! Endpoint: http://alb-infrapilot-${dep.env}.ap-south-1.elb.amazonaws.com`, 'success');
  dep.status = 'deployed';
  dep.endpoint = `http://alb-infrapilot-${dep.env}.ap-south-1.elb.amazonaws.com`;
  renderDeployments();
  state.pipelineRunning = false;
}

// User Approval Promise Resolver
function waitForApproval(modal, dep) {
  return new Promise((resolve) => {
    modal.classList.add('open');
    document.getElementById('modal-dep-details').textContent = `${dep.project} (${dep.env}) - Cost: ${dep.cost}`;

    const approveBtn = document.getElementById('modal-approve-btn');
    const rejectBtn = document.getElementById('modal-reject-btn');

    function onApprove() {
      cleanup();
      modal.classList.remove('open');
      resolve(true);
    }

    function onReject() {
      cleanup();
      modal.classList.remove('open');
      resolve(false);
    }

    function cleanup() {
      approveBtn.removeEventListener('click', onApprove);
      rejectBtn.removeEventListener('click', onReject);
    }

    approveBtn.addEventListener('click', onApprove);
    rejectBtn.addEventListener('click', onReject);
  });
}

// View Pipeline Details from table
function viewPipelineDetails(depId) {
  switchView('pipelines');
  const dep = state.deployments.find(d => d.id === depId);
  if (dep) {
    const terminal = document.getElementById('pipeline-terminal');
    terminal.innerHTML = `
      <div class="log-line"><span class="log-time">[ARCHIVED]</span> <span class="log-info">Loaded deployment logs for ${dep.id} (${dep.project})</span></div>
      <div class="log-line"><span class="log-time">[ARCHIVED]</span> <span class="log-success">Target: ${dep.provider} | Template: ${dep.template} | Cost: ${dep.cost}</span></div>
      <div class="log-line"><span class="log-time">[ARCHIVED]</span> <span class="log-success">Endpoint: ${dep.endpoint}</span></div>
      <div class="log-line"><span class="log-time">[ARCHIVED]</span> <span class="log-info">Status: ${dep.status.toUpperCase()}</span></div>
    `;
  }
}

// Check Backend Health
async function checkBackendHealth() {
  const indicator = document.getElementById('backend-status-indicator');
  try {
    const res = await fetch('http://localhost:8080/api/v1/health', { method: 'GET' });
    if (res.ok) {
      const data = await res.json();
      state.backendConnected = true;
      if (indicator) {
        indicator.innerHTML = `<span class="pulse-dot"></span> Spring Boot API: Online`;
      }
    } else {
      setLocalDemoMode(indicator);
    }
  } catch (err) {
    setLocalDemoMode(indicator);
  }
}

function setLocalDemoMode(indicator) {
  if (indicator) {
    indicator.innerHTML = `<span class="pulse-dot" style="background-color: var(--accent-cyan); box-shadow: 0 0 8px var(--accent-cyan);"></span> InfraPilot: Ready`;
  }
}

// ==========================================
// AUTHENTICATION & USER PROFILE PANEL
// ==========================================

const authState = {
  user: {
    name: "Chaitanya",
    email: "chaitanya@infrapilot.dev",
    role: "DEVOPS LEAD",
    avatar: "C",
    color: "linear-gradient(135deg, #6366F1, #8B5CF6)"
  },
  sessionId: "sess-" + Math.random().toString(36).substring(2, 10),
  loginTime: new Date(),
  history: [
    { time: "Today, 10:30 AM", ip: "127.0.0.1 (Localhost)", agent: "Chrome 122 (Windows 11)", success: true },
    { time: "Yesterday, 06:15 PM", ip: "10.0.1.4 (VPN WireGuard)", agent: "Chrome 122 (Windows 11)", success: true },
    { time: "22 Oct, 09:12 AM", ip: "192.168.1.105 (Office WiFi)", agent: "Safari 17 (macOS)", success: true },
    { time: "20 Oct, 11:45 PM", ip: "185.220.101.5 (Blocked Tor Exit)", agent: "Unknown Bot", success: false }
  ]
};

function setupAuth() {
  const loginPage = document.getElementById('login-page');
  const appShell = document.getElementById('app-shell');
  const loginForm = document.getElementById('login-form');
  const togglePassBtn = document.getElementById('toggle-password');
  const passInput = document.getElementById('login-password');

  // Check saved session in localStorage
  const savedUser = localStorage.getItem('infrapilot_auth');
  if (savedUser) {
    if (loginPage) loginPage.style.display = 'none';
    if (appShell) appShell.style.display = 'flex';
  } else {
    // Default show login page first
    if (loginPage) loginPage.style.display = 'flex';
    if (appShell) appShell.style.display = 'none';
  }

  // Password Visibility Toggle
  if (togglePassBtn && passInput) {
    togglePassBtn.addEventListener('click', () => {
      const isPass = passInput.getAttribute('type') === 'password';
      passInput.setAttribute('type', isPass ? 'text' : 'password');
    });
  }

  // Login Form Submission
  if (loginForm) {
    loginForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const email = document.getElementById('login-email').value;
      const submitBtn = document.getElementById('login-submit-btn');
      const errorDiv = document.getElementById('login-error');

      if (submitBtn) submitBtn.classList.add('loading');
      if (errorDiv) errorDiv.textContent = '';

      // Simulate authentication check
      setTimeout(() => {
        if (submitBtn) submitBtn.classList.remove('loading');

        // Extract name from email
        const username = email.split('@')[0] || "Chaitanya";
        authState.user.email = email;
        authState.user.name = username.charAt(0).toUpperCase() + username.slice(1);
        authState.loginTime = new Date();
        authState.sessionId = "sess-" + Math.random().toString(36).substring(2, 10);

        // Record history
        authState.history.unshift({
          time: "Just now",
          ip: "127.0.0.1 (Localhost)",
          agent: navigator.userAgent.includes("Chrome") ? "Chrome (Windows)" : "Web Browser",
          success: true
        });

        // Record Audit Event
        state.auditEvents.unshift({
          timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
          user: `${authState.user.name.toLowerCase()} (${authState.user.role.toLowerCase()})`,
          action: "USER_AUTHENTICATED",
          resource: "oauth2/session/" + authState.sessionId,
          ip: "127.0.0.1"
        });
        renderAuditTable();

        // Save session
        localStorage.setItem('infrapilot_auth', JSON.stringify(authState.user));

        // Switch to App Shell
        if (loginPage) loginPage.style.display = 'none';
        if (appShell) appShell.style.display = 'flex';

        updateUserProfileDisplay();
      }, 700);
    });
  }

  // Setup outside click listener for user dropdown
  document.addEventListener('click', (e) => {
    const userPanel = document.getElementById('user-panel');
    const userBadge = document.getElementById('user-badge-toggle');
    if (!userPanel || !userBadge) return;

    if (!userBadge.contains(e.target) && !userPanel.contains(e.target)) {
      userPanel.classList.remove('open');
    }
  });

  updateUserProfileDisplay();
  startSessionDurationTimer();
}

// Social SSO Login handler
window.socialLogin = function(provider) {
  const loginPage = document.getElementById('login-page');
  const appShell = document.getElementById('app-shell');

  authState.user.name = provider === 'github' ? "Chaitanya (GitHub)" : "Chaitanya (Google)";
  authState.user.email = provider === 'github' ? "chaitanya@users.noreply.github.com" : "chaitanya@google.com";
  authState.loginTime = new Date();
  authState.sessionId = "sess-sso-" + Math.random().toString(36).substring(2, 8);

  state.auditEvents.unshift({
    timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
    user: `${authState.user.name.toLowerCase()}`,
    action: `SSO_LOGIN_${provider.toUpperCase()}`,
    resource: "oauth2/sso/" + provider,
    ip: "127.0.0.1"
  });
  renderAuditTable();

  localStorage.setItem('infrapilot_auth', JSON.stringify(authState.user));
  if (loginPage) loginPage.style.display = 'none';
  if (appShell) appShell.style.display = 'flex';
  updateUserProfileDisplay();
};

// Toggle User Profile Dropdown Panel
window.toggleUserPanel = function() {
  const panel = document.getElementById('user-panel');
  if (panel) {
    panel.classList.toggle('open');
    updateUserProfileDisplay();
  }
};

window.closeUserPanel = function() {
  const panel = document.getElementById('user-panel');
  if (panel) panel.classList.remove('open');
};

// Logout User
window.logoutUser = function() {
  localStorage.removeItem('infrapilot_auth');
  const loginPage = document.getElementById('login-page');
  const appShell = document.getElementById('app-shell');
  const userPanel = document.getElementById('user-panel');

  state.auditEvents.unshift({
    timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
    user: `${authState.user.name.toLowerCase()}`,
    action: "USER_LOGGED_OUT",
    resource: "oauth2/revoke/" + authState.sessionId,
    ip: "127.0.0.1"
  });
  renderAuditTable();

  if (userPanel) userPanel.classList.remove('open');
  if (appShell) appShell.style.display = 'none';
  if (loginPage) loginPage.style.display = 'flex';
};

function updateUserProfileDisplay() {
  const sessionEl = document.getElementById('up-session-id');
  const loginTimeEl = document.getElementById('up-login-time');
  const browserEl = document.getElementById('up-browser');
  const historyEl = document.getElementById('up-history');

  if (sessionEl) sessionEl.textContent = authState.sessionId;
  if (loginTimeEl) {
    const hours = authState.loginTime.getHours().toString().padStart(2, '0');
    const mins = authState.loginTime.getMinutes().toString().padStart(2, '0');
    loginTimeEl.textContent = `Today at ${hours}:${mins}`;
  }
  if (browserEl) {
    browserEl.textContent = navigator.userAgent.includes("Chrome") ? "Chrome 122 (x86_64)" : "Modern Web Browser";
  }

  if (historyEl) {
    historyEl.innerHTML = authState.history.map(item => `
      <div class="up-history-item">
        <span class="up-history-dot ${item.success ? 'success' : 'failed'}"></span>
        <div class="up-history-info">
          <div class="up-history-text"><strong>${item.success ? 'Successful Authentication' : 'Failed Attempt Blocked'}</strong></div>
          <div class="up-history-meta">${item.time} · ${item.ip}</div>
        </div>
      </div>
    `).join('');
  }
}

function startSessionDurationTimer() {
  const durEl = document.getElementById('up-session-duration');
  if (!durEl) return;

  setInterval(() => {
    const diffSec = Math.floor((new Date() - authState.loginTime) / 1000);
    const m = Math.floor(diffSec / 60);
    const s = diffSec % 60;
    durEl.textContent = `${m}m ${s}s`;
  }, 1000);
}


// ==========================================
// GITHUB REPOSITORY SECURITY SCANNER ENGINE
// ==========================================

const githubSecurityDB = [
  {
    id: "SEC-101",
    category: "iac",
    scanner: "Checkov",
    ruleId: "CKV_AWS_135",
    severity: "HIGH",
    title: "EBS Volume Encryption by Default Disabled",
    file: "infrastructure/terraform/modules/rds/main.tf:42",
    description: "AWS EBS encryption by default is not enabled. Any unattached or attached block storage volumes created without explicit encryption configuration may expose unencrypted data at rest.",
    diffBefore: `resource "aws_ebs_volume" "app_data" {\n  availability_zone = "ap-south-1a"\n  size              = 50\n  # Missing encryption block\n}`,
    diffAfter: `resource "aws_ebs_volume" "app_data" {\n  availability_zone = "ap-south-1a"\n  size              = 50\n+ encrypted         = true\n+ kms_key_id        = aws_kms_key.infrapilot.arn\n}`,
    remediation: "Set \`encrypted = true\` and specify a customer-managed KMS key ARN.",
    fixed: false
  },
  {
    id: "SEC-102",
    category: "iac",
    scanner: "Checkov",
    ruleId: "CKV_AWS_20",
    severity: "MEDIUM",
    title: "S3 Bucket Public Access Block Settings Incomplete",
    file: "infrastructure/terraform/modules/s3-artifacts/main.tf:18",
    description: "S3 bucket lacks \`aws_s3_bucket_public_access_block\` configuration. Public access might be inadvertently permitted through IAM or ACL permissions.",
    diffBefore: `resource "aws_s3_bucket" "artifacts" {\n  bucket = "infrapilot-pipeline-artifacts"\n}`,
    diffAfter: `resource "aws_s3_bucket_public_access_block" "artifacts" {\n  bucket                  = aws_s3_bucket.artifacts.id\n+ block_public_acls       = true\n+ block_public_policy     = true\n+ ignore_public_acls      = true\n+ restrict_public_buckets = true\n}`,
    remediation: "Add an \`aws_s3_bucket_public_access_block\` resource with all 4 guardrails enabled.",
    fixed: false
  },
  {
    id: "SEC-103",
    category: "sast",
    scanner: "Semgrep",
    ruleId: "SEMGREP_SQL_INJECTION",
    severity: "MEDIUM",
    title: "Concatenated SQL String Query in JPA Repository",
    file: "backend/src/main/java/com/infrapilot/repository/DeploymentQueryBuilder.java:64",
    description: "Detected potential SQL injection vulnerability in dynamic query construction. User parameters are concatenated into raw JPQL query strings rather than bound as positional or named parameters.",
    diffBefore: `- String jpql = "SELECT d FROM Deployment d WHERE d.env = '" + envName + "'";\n- return entityManager.createQuery(jpql).getResultList();`,
    diffAfter: `+ String jpql = "SELECT d FROM Deployment d WHERE d.env = :envName";\n+ return entityManager.createQuery(jpql, Deployment.class)\n+     .setParameter("envName", envName)\n+     .getResultList();`,
    remediation: "Use parameterized queries or Spring Data JPA \`@Param\` annotations instead of string concatenation.",
    fixed: false
  },
  {
    id: "SEC-104",
    category: "secrets",
    scanner: "Gitleaks",
    ruleId: "GITLEAKS_AWS_SECRET",
    severity: "CRITICAL",
    title: "Hardcoded AWS Access Key ID Detected",
    file: "scripts/deploy-test-runner.sh:12",
    description: "Pattern matching regex \`AKIA[0-9A-Z]{16}\` was detected. Hardcoded cloud provider credentials can lead to unauthorized cloud account compromise and resource takeover.",
    diffBefore: `- export AWS_ACCESS_KEY_ID="AKIAIOSFODNN7EXAMPLE"\n- export AWS_SECRET_ACCESS_KEY="wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"`,
    diffAfter: `+ export AWS_ACCESS_KEY_ID="\${AWS_ACCESS_KEY_ID:?Required}"\n+ export AWS_SECRET_ACCESS_KEY="\${AWS_SECRET_ACCESS_KEY:?Required}"`,
    remediation: "Immediately rotate this credential in AWS IAM. Pass credentials using IAM Roles or HashiCorp Vault dev token.",
    fixed: true
  },
  {
    id: "SEC-105",
    category: "cve",
    scanner: "Trivy",
    ruleId: "CVE-2023-44487",
    severity: "HIGH",
    title: "HTTP/2 Rapid Reset Denial of Service (RST_STREAM)",
    file: "backend/pom.xml:88 (io.netty:netty-codec-http2:4.1.94.Final)",
    description: "The HTTP/2 protocol allows a denial of service (server resource consumption) because request cancellation can reset many streams quickly with low cost to the client.",
    diffBefore: `- <version>4.1.94.Final</version>`,
    diffAfter: `+ <version>4.1.100.Final</version>`,
    remediation: "Upgrade Netty to version 4.1.100.Final or higher to enforce stream reset rate limiting.",
    fixed: true
  },
  {
    id: "SEC-106",
    category: "posture",
    scanner: "Scorecard",
    ruleId: "GH_NO_BRANCH_PROTECTION",
    severity: "HIGH",
    title: "Branch Protection Not Enforced on 'main'",
    file: ".github/workflows/deploy.yml:1",
    description: "The default branch \`main\` allows direct pushes without requiring pull request reviews, status checks passing, or linear history.",
    diffBefore: `# Branch protection disabled in GitHub Settings`,
    diffAfter: `+ Required PR Approvals: >= 1\n+ Require Signed Commits: True\n+ Require Status Checks (Gitleaks, Checkov, Trivy) to pass`,
    remediation: "Enable GitHub Branch Protection rule on \`main\` with minimum 1 review and required CI status checks.",
    fixed: true
  }
];

let activeFindingFilter = "all";
let activeFindingSearch = "";

window.selectDemoRepo = function(repoName) {
  const input = document.getElementById('gh-repo-url');
  if (input) input.value = `https://github.com/${repoName}`;

  document.querySelectorAll('.gh-preset-btn').forEach(btn => {
    btn.classList.toggle('active', btn.textContent.trim() === repoName.split('/')[1]);
  });
};

window.startGithubSecurityScan = function() {
  const btn = document.getElementById('gh-run-scan-btn');
  const btnText = document.getElementById('gh-scan-btn-text');
  const terminal = document.getElementById('gh-scan-terminal');
  const logsEl = document.getElementById('gh-terminal-logs');
  const titleEl = document.getElementById('gh-terminal-title');
  const progressBar = document.getElementById('gh-progress-bar');
  const elapsedEl = document.getElementById('gh-scan-elapsed');
  const repoUrl = document.getElementById('gh-repo-url').value;
  const branch = document.getElementById('gh-repo-branch').value;
  const profile = document.getElementById('gh-scan-profile').value;

  if (btn) btn.classList.add('running');
  if (btnText) btnText.textContent = "Scanning...";
  if (terminal) terminal.style.display = 'block';
  if (logsEl) logsEl.innerHTML = '';

  const startTime = Date.now();
  const timer = setInterval(() => {
    if (elapsedEl) {
      elapsedEl.textContent = ((Date.now() - startTime) / 1000).toFixed(1) + "s";
    }
  }, 100);

  const steps = [
    { p: 10, title: "Cloning repository AST...", text: `[1/6] 📦 Cloning tree from ${repoUrl} (ref: ${branch})...`, type: "info" },
    { p: 25, title: "Gitleaks secret detection...", text: `[2/6] 🔑 Initializing Gitleaks v8.18.1 with 85 default + InfraPilot custom rules...`, type: "info" },
    { p: 40, title: "Gitleaks secret audit passed...", text: `[Gitleaks] Scanned 148 commits, 432 files. 0 active secrets detected. PASSED.`, type: "pass" },
    { p: 55, title: "Running Checkov & tfsec...", text: `[3/6] 🛡️ Evaluating Checkov against Terraform HCL & docker-compose.yml...`, type: "info" },
    { p: 70, title: "Checkov findings found...", text: `[Checkov] Discovered 2 warnings: CKV_AWS_135 (EBS encryption), CKV_AWS_20 (S3 public block).`, type: "warn" },
    { p: 82, title: "Auditing dependencies with Trivy...", text: `[4/6] 📦 Running Trivy CVE audit on pom.xml & container base images... 0 CRITICAL vulnerabilities.`, type: "pass" },
    { p: 92, title: "Running Semgrep SAST...", text: `[5/6] ⚡ Scanning code with Semgrep (OWASP Top 10 ruleset)... 1 Medium SQL parameter issue.`, type: "warn" },
    { p: 100, title: "GitHub Posture & Scorecard...", text: `[6/6] 📜 Evaluating GitHub Supply Chain & Branch Rules... Score: 94/100 (Grade A). Complete!`, type: "pass" }
  ];

  let stepIdx = 0;
  function runStep() {
    if (stepIdx < steps.length) {
      const s = steps[stepIdx];
      if (titleEl) titleEl.textContent = s.title;
      if (progressBar) progressBar.style.width = s.p + "%";

      if (logsEl) {
        const row = document.createElement('div');
        row.className = 'gh-log-row';
        row.innerHTML = `
          <span class="gh-log-time">[${new Date().toLocaleTimeString()}]</span>
          <span class="gh-log-${s.type}">${s.text}</span>
        `;
        logsEl.appendChild(row);
        logsEl.scrollTop = logsEl.scrollHeight;
      }
      stepIdx++;
      setTimeout(runStep, 380);
    } else {
      clearInterval(timer);
      if (btn) btn.classList.remove('running');
      if (btnText) btnText.textContent = "Re-Run Security Scan";
      if (titleEl) titleEl.textContent = "Scan Complete — 148 Files Audited";

      // Re-render score & findings
      refreshSecurityScoreAndFindings();
    }
  }

  runStep();
};

window.filterFindings = function(category) {
  activeFindingFilter = category;
  document.querySelectorAll('.ftab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-filter') === category);
  });
  renderFindingsList();
};

window.onSearchFindings = function(query) {
  activeFindingSearch = query.toLowerCase();
  renderFindingsList();
};

window.applyFindingFix = function(id) {
  const finding = githubSecurityDB.find(f => f.id === id);
  if (finding) {
    finding.fixed = true;
    refreshSecurityScoreAndFindings();

    // Log to Audit Trail
    state.auditEvents.unshift({
      timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
      user: `${authState.user.name.toLowerCase()} (${authState.user.role.toLowerCase()})`,
      action: "SECURITY_REMEDIATION_APPLIED",
      resource: `${finding.ruleId} (${finding.file})`,
      ip: "127.0.0.1"
    });
    renderAuditTable();
  }
};

function refreshSecurityScoreAndFindings() {
  const activeFindings = githubSecurityDB.filter(f => !f.fixed);
  const critical = activeFindings.filter(f => f.severity === 'CRITICAL').length;
  const high = activeFindings.filter(f => f.severity === 'HIGH').length;
  const medium = activeFindings.filter(f => f.severity === 'MEDIUM').length;
  const passed = 24 + githubSecurityDB.filter(f => f.fixed).length;

  // Compute Score (100 - critical*25 - high*5 - medium*2)
  let score = 100 - (critical * 25) - (high * 5) - (medium * 2);
  if (score < 0) score = 0;

  // Update Score Elements
  const scorePath = document.getElementById('gh-score-circle-path');
  const scoreText = document.getElementById('gh-score-text');
  const scoreGrade = document.getElementById('gh-score-grade');
  const scoreMeta = document.getElementById('gh-score-meta');

  if (scorePath) scorePath.setAttribute('stroke-dasharray', `${score}, 100`);
  if (scoreText) scoreText.textContent = score;

  if (scoreGrade) {
    if (score >= 90) { scoreGrade.textContent = "GRADE A"; scoreGrade.style.color = "var(--success)"; }
    else if (score >= 75) { scoreGrade.textContent = "GRADE B"; scoreGrade.style.color = "var(--warning)"; }
    else { scoreGrade.textContent = "GRADE C"; scoreGrade.style.color = "var(--danger)"; }
  }

  if (scoreMeta) {
    scoreMeta.textContent = `148 files audited · ${critical} Critical · ${high} High`;
  }

  // Update Counters
  const cEl = document.getElementById('gh-count-critical');
  const hEl = document.getElementById('gh-count-high');
  const mEl = document.getElementById('gh-count-medium');
  const pEl = document.getElementById('gh-count-passed');

  if (cEl) cEl.textContent = critical;
  if (hEl) hEl.textContent = high;
  if (mEl) mEl.textContent = medium;
  if (pEl) pEl.textContent = passed;

  // Update Tab counts
  const tabAll = document.getElementById('tab-count-all');
  const tabSecrets = document.getElementById('tab-count-secrets');
  const tabIac = document.getElementById('tab-count-iac');
  const tabCve = document.getElementById('tab-count-cve');
  const tabSast = document.getElementById('tab-count-sast');
  const tabPosture = document.getElementById('tab-count-posture');

  if (tabAll) tabAll.textContent = activeFindings.length;
  if (tabSecrets) tabSecrets.textContent = activeFindings.filter(f => f.category === 'secrets').length;
  if (tabIac) tabIac.textContent = activeFindings.filter(f => f.category === 'iac').length;
  if (tabCve) tabCve.textContent = activeFindings.filter(f => f.category === 'cve').length;
  if (tabSast) tabSast.textContent = activeFindings.filter(f => f.category === 'sast').length;
  if (tabPosture) tabPosture.textContent = activeFindings.filter(f => f.category === 'posture').length;

  renderFindingsList();
}

function renderFindingsList() {
  const container = document.getElementById('findings-list-container');
  if (!container) return;

  const filtered = githubSecurityDB.filter(f => {
    if (f.fixed) return false;
    if (activeFindingFilter !== 'all' && f.category !== activeFindingFilter) return false;
    if (activeFindingSearch) {
      const match = f.title.toLowerCase().includes(activeFindingSearch) ||
                    f.ruleId.toLowerCase().includes(activeFindingSearch) ||
                    f.file.toLowerCase().includes(activeFindingSearch) ||
                    f.description.toLowerCase().includes(activeFindingSearch);
      if (!match) return false;
    }
    return true;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="empty-findings">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--success); margin-bottom: 10px;"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <p style="font-size: 1rem; font-weight: 600; color: var(--text-main);">No Unresolved Findings in this Category!</p>
        <p style="color: var(--text-dim); font-size: 0.8rem; margin-top: 4px;">All security elements for this filter comply with DevSecOps governance policies.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(f => {
    let severityClass = 'severity-medium';
    let badgeClass = 'badge-warning';
    if (f.severity === 'CRITICAL') { severityClass = 'severity-critical'; badgeClass = 'badge-danger'; }
    if (f.severity === 'HIGH') { severityClass = 'severity-high'; badgeClass = 'badge-warning'; }

    return `
      <div class="finding-card ${severityClass}">
        <div class="finding-header">
          <div class="finding-tags">
            <span class="badge ${badgeClass}">${f.severity}</span>
            <span class="finding-scanner-badge">${f.scanner}</span>
            <span class="finding-rule-id">${f.ruleId}</span>
            <span class="finding-file-tag">📁 ${f.file}</span>
          </div>
          <div>
            <button class="fix-apply-btn" onclick="applyFindingFix('${f.id}')">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              Auto-Apply Fix
            </button>
          </div>
        </div>

        <div style="font-weight: 600; font-size: 0.95rem; color: var(--text-main); margin-bottom: 6px;">
          ${f.title}
        </div>
        <div class="finding-desc">
          ${f.description}
        </div>

        <!-- Code Patch Preview -->
        <div class="finding-fix-box">
          <div class="finding-fix-header">
            <span>RECOMMENDED REMEDIATION DIFF</span>
            <span>${f.file.split(':')[0]}</span>
          </div>
          <div class="finding-diff-code"><span class="diff-del">${escapeHtml(f.diffBefore)}</span><span class="diff-add">${escapeHtml(f.diffAfter)}</span></div>
        </div>
      </div>
    `;
  }).join('');
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

// Export Security Reports (SARIF or JSON)
window.exportSecurityReport = function(format) {
  const repoUrl = document.getElementById('gh-repo-url')?.value || "InfraPilot";
  let content = "";
  let filename = "";
  let mimeType = "";

  if (format === 'sarif') {
    const sarifReport = {
      "$schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
      "version": "2.1.0",
      "runs": [
        {
          "tool": {
            "driver": {
              "name": "InfraPilot-Security-Engine",
              "version": "2.4.1",
              "informationUri": "https://github.com/InfraPilot/InfraPilot",
              "rules": githubSecurityDB.map(f => ({
                "id": f.ruleId,
                "name": f.title,
                "shortDescription": { "text": f.description },
                "defaultConfiguration": { "level": f.severity === 'CRITICAL' ? 'error' : 'warning' }
              }))
            }
          },
          "results": githubSecurityDB.filter(f => !f.fixed).map(f => ({
            "ruleId": f.ruleId,
            "message": { "text": f.description },
            "locations": [
              {
                "physicalLocation": {
                  "artifactLocation": { "uri": f.file.split(':')[0] },
                  "region": { "startLine": parseInt(f.file.split(':')[1]) || 1 }
                }
              }
            ]
          }))
        }
      ]
    };
    content = JSON.stringify(sarifReport, null, 2);
    filename = `infrapilot-security-scan-${Date.now()}.sarif`;
    mimeType = "application/json";
  } else {
    const jsonAudit = {
      platform: "InfraPilot DevSecOps",
      repository: repoUrl,
      timestamp: new Date().toISOString(),
      auditor: authState.user.email,
      findings: githubSecurityDB
    };
    content = JSON.stringify(jsonAudit, null, 2);
    filename = `infrapilot-audit-${Date.now()}.json`;
    mimeType = "application/json";
  }

  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
};

// Wire everything to DOMContentLoaded
document.addEventListener('DOMContentLoaded', () => {
  setupAuth();
  refreshSecurityScoreAndFindings();
});

