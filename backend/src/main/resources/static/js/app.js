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

// Initialize app on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  setupNavigation();
  setupDeploymentWizard();
  renderDeployments();
  renderSecurityTable();
  renderAuditTable();
  checkBackendHealth();
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
