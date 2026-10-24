// ==============================================================================
// InfraPilot Admin & Operations Portal — Logic & Data Management Engine
// ==============================================================================

const adminState = {
  activeTab: 'overview',
  roleFilter: 'ALL',
  loginFilter: 'ALL',
  userSearch: '',
  loginSearch: '',

  // In-memory data matching PostgreSQL V1 & V2 schema
  users: [
    {
      id: "usr-01",
      name: "Chaitanya",
      email: "chaitanya@infrapilot.dev",
      role: "ADMIN",
      mfa: true,
      status: "ACTIVE",
      lastActive: "Just now",
      avatar: "C",
      color: "linear-gradient(135deg, #6366F1, #8B5CF6)"
    },
    {
      id: "usr-02",
      name: "Priya Sharma",
      email: "priya@infrapilot.dev",
      role: "DEVELOPER",
      mfa: true,
      status: "ACTIVE",
      lastActive: "2 mins ago",
      avatar: "P",
      color: "linear-gradient(135deg, #6366F1, #8B5CF6)"
    },
    {
      id: "usr-03",
      name: "Arjun Patel",
      email: "arjun@infrapilot.dev",
      role: "DEVOPS",
      mfa: true,
      status: "ACTIVE",
      lastActive: "5 mins ago",
      avatar: "A",
      color: "linear-gradient(135deg, #3B82F6, #06B6D4)"
    },
    {
      id: "usr-04",
      name: "Sneha Rao",
      email: "sneha@infrapilot.dev",
      role: "DEVELOPER",
      mfa: false,
      status: "ACTIVE",
      lastActive: "8 mins ago",
      avatar: "S",
      color: "linear-gradient(135deg, #F59E0B, #EF4444)"
    }
  ],

  sessions: [
    {
      id: "sess-a7b2c9e1",
      userId: "usr-01",
      userName: "Chaitanya",
      ip: "127.0.0.1 (Localhost)",
      device: "Chrome 122 (Windows 11)",
      loginTime: "Today at 22:15",
      expiresIn: "23h 45m",
      status: "ACTIVE"
    },
    {
      id: "sess-f4e8d1a0",
      userId: "usr-02",
      userName: "Priya Sharma",
      ip: "10.0.1.4 (WireGuard VPN)",
      device: "VS Code / Git CLI (macOS)",
      loginTime: "Today at 20:30",
      expiresIn: "21h 15m",
      status: "ACTIVE"
    },
    {
      id: "sess-b9c312fa",
      userId: "usr-03",
      userName: "Arjun Patel",
      ip: "192.168.1.105 (Office WiFi)",
      device: "Firefox 123 (Linux Ubuntu)",
      loginTime: "Today at 18:40",
      expiresIn: "19h 20m",
      status: "ACTIVE"
    }
  ],

  logins: [
    {
      time: "2026-10-24 22:15:30",
      user: "chaitanya@infrapilot.dev",
      ip: "127.0.0.1 (Localhost)",
      agent: "Mozilla/5.0 (Windows NT 10.0; Win64)",
      status: "SUCCESS",
      details: "OAuth2 Password Grant (MFA Verified)"
    },
    {
      time: "2026-10-24 21:40:12",
      user: "root@infrapilot.dev",
      ip: "185.220.101.5 (Tor Exit)",
      agent: "curl/7.88.1 (automated bot)",
      status: "BLOCKED_IP",
      details: "Brute-force detected. IP banned in Redis rate limiter."
    },
    {
      time: "2026-10-24 20:30:15",
      user: "priya@infrapilot.dev",
      ip: "10.0.1.4 (VPN WireGuard)",
      agent: "Git/2.43.0 (Darwin x86_64)",
      status: "SUCCESS",
      details: "Personal Access Token authentication"
    },
    {
      time: "2026-10-24 18:40:02",
      user: "arjun@infrapilot.dev",
      ip: "192.168.1.105 (Office LAN)",
      agent: "Firefox 123 (Ubuntu Linux)",
      status: "SUCCESS",
      details: "Web Dashboard Session login"
    },
    {
      time: "2026-10-24 14:12:44",
      user: "sneha@infrapilot.dev",
      ip: "10.0.1.8 (VPN WireGuard)",
      agent: "Chrome 122 (macOS)",
      status: "FAILED_PASSWORD",
      details: "Invalid password attempt (1 of 5)"
    },
    {
      time: "2026-10-24 14:13:10",
      user: "sneha@infrapilot.dev",
      ip: "10.0.1.8 (VPN WireGuard)",
      agent: "Chrome 122 (macOS)",
      status: "SUCCESS",
      details: "Password corrected & session initiated"
    }
  ],

  activities: [
    {
      actor: "Priya Sharma",
      avatar: "P",
      color: "linear-gradient(135deg, #6366F1, #8B5CF6)",
      action: "pushed 3 commits to",
      target: "payment-gateway (feature-auth)",
      time: "2 mins ago",
      details: "commit b8c3d9a: add idempotency header to stripe checkout handler"
    },
    {
      actor: "Arjun Patel",
      avatar: "A",
      color: "linear-gradient(135deg, #3B82F6, #06B6D4)",
      action: "triggered Terraform apply for",
      target: "analytics-worker (staging)",
      time: "5 mins ago",
      details: "AWS ap-south-1: provisioned ECS task definition v4 and CloudWatch alarms"
    },
    {
      actor: "Sneha Rao",
      avatar: "S",
      color: "linear-gradient(135deg, #F59E0B, #EF4444)",
      action: "reviewed and approved PR #142 on",
      target: "ecommerce-core/main",
      time: "8 mins ago",
      details: "LGTM: verified Checkov IaC security scan passed without CRITICAL warnings"
    },
    {
      actor: "Chaitanya",
      avatar: "C",
      color: "linear-gradient(135deg, #6366F1, #8B5CF6)",
      action: "applied security remediation",
      target: "CKV_AWS_135 (EBS encryption)",
      time: "15 mins ago",
      details: "Automated fix applied to infrastructure/terraform/modules/rds/main.tf"
    },
    {
      actor: "Priya Sharma",
      avatar: "P",
      color: "linear-gradient(135deg, #6366F1, #8B5CF6)",
      action: "merged branch into main",
      target: "hotfix/db-connection-pool",
      time: "22 mins ago",
      details: "HikariCP maximumPoolSize adjusted to 20 for PostgreSQL 15 failover"
    }
  ]
};

// Initialize Admin UI
document.addEventListener('DOMContentLoaded', () => {
  renderOverviewLogins();
  renderOverviewActivities();
  renderUsersTable();
  renderSessionsTable();
  renderLoginsTable();
  renderFullActivitiesTimeline();
  updateHeaderNodeIp();
});

function updateHeaderNodeIp() {
  const ipEl = document.getElementById('server-host-ip');
  if (ipEl && window.location.host) {
    ipEl.textContent = window.location.host;
  }
}

// Navigation Tab Switcher
window.switchAdminTab = function(tabName) {
  adminState.activeTab = tabName;

  document.querySelectorAll('.nav-tab').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-view') === tabName);
  });

  document.querySelectorAll('.admin-view').forEach(view => {
    view.classList.remove('active-view');
  });

  const targetView = document.getElementById(`view-${tabName}`);
  if (targetView) targetView.classList.add('active-view');
};

// ==========================================
// RENDERERS
// ==========================================

function renderOverviewLogins() {
  const tbody = document.getElementById('overview-logins-tbody');
  if (!tbody) return;

  tbody.innerHTML = adminState.logins.slice(0, 4).map(l => {
    let badgeClass = 'badge-success';
    if (l.status === 'BLOCKED_IP') badgeClass = 'badge-danger';
    if (l.status === 'FAILED_PASSWORD') badgeClass = 'badge-warning';

    return `
      <tr>
        <td><strong>${l.user}</strong></td>
        <td><code>${l.ip.split(' ')[0]}</code></td>
        <td><span class="badge ${badgeClass}">${l.status}</span></td>
        <td><small style="color: var(--text-dim);">${l.time.split(' ')[1]}</small></td>
      </tr>
    `;
  }).join('');
}

function renderOverviewActivities() {
  const container = document.getElementById('overview-activities-feed');
  if (!container) return;

  container.innerHTML = adminState.activities.slice(0, 4).map(a => `
    <div class="timeline-item">
      <div class="tl-avatar" style="background: ${a.color};">${a.avatar}</div>
      <div class="tl-content">
        <div class="tl-head">
          <span class="tl-text"><strong>${a.actor}</strong> ${a.action} <strong>${a.target}</strong></span>
          <span class="tl-time">${a.time}</span>
        </div>
        <div class="tl-details">${a.details}</div>
      </div>
    </div>
  `).join('');
}

function renderUsersTable() {
  const tbody = document.getElementById('users-table-tbody');
  if (!tbody) return;

  const filtered = adminState.users.filter(u => {
    if (adminState.roleFilter !== 'ALL' && u.role !== adminState.roleFilter) return false;
    if (adminState.userSearch) {
      const q = adminState.userSearch;
      if (!u.name.toLowerCase().includes(q) && !u.email.toLowerCase().includes(q) && !u.role.toLowerCase().includes(q)) {
        return false;
      }
    }
    return true;
  });

  tbody.innerHTML = filtered.map(u => `
    <tr>
      <td>
        <div style="display: flex; align-items: center; gap: 10px;">
          <div class="admin-avatar" style="background: ${u.color}; width: 28px; height: 28px; font-size: 0.75rem;">${u.avatar}</div>
          <strong>${u.name}</strong>
        </div>
      </td>
      <td><code>${u.email}</code></td>
      <td><span class="badge badge-info">${u.role}</span></td>
      <td>${u.mfa ? '<span class="badge badge-success">MFA ON</span>' : '<span class="badge badge-warning">MFA OFF</span>'}</td>
      <td>
        <span class="badge ${u.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'}">${u.status}</span>
      </td>
      <td><small style="color: var(--text-dim);">${u.lastActive}</small></td>
      <td>
        <div style="display: flex; gap: 6px;">
          <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 0.72rem;" onclick="toggleUserStatus('${u.id}')">
            ${u.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
          </button>
          <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 0.72rem;" onclick="resetPasswordPrompt('${u.email}')">
            Reset
          </button>
        </div>
      </td>
    </tr>
  `).join('');

  // Update counters
  const badgeEl = document.getElementById('badge-total-users');
  const statEl = document.getElementById('stat-total-users');
  if (badgeEl) badgeEl.textContent = adminState.users.length;
  if (statEl) statEl.textContent = adminState.users.length;
}

function renderSessionsTable() {
  const tbody = document.getElementById('sessions-table-tbody');
  if (!tbody) return;

  tbody.innerHTML = adminState.sessions.map(s => `
    <tr>
      <td><strong>${s.userName}</strong></td>
      <td><code>${s.id}</code></td>
      <td><code>${s.ip}</code></td>
      <td>${s.device}</td>
      <td>${s.loginTime}</td>
      <td><small style="color: var(--accent-cyan); font-family: var(--font-mono);">${s.expiresIn}</small></td>
      <td><span class="badge badge-success">${s.status}</span></td>
      <td>
        <button class="btn btn-danger" style="padding: 4px 10px; font-size: 0.72rem;" onclick="revokeSession('${s.id}')">
          Revoke
        </button>
      </td>
    </tr>
  `).join('');

  const liveBadge = document.getElementById('badge-live-sessions');
  const statLive = document.getElementById('stat-live-sessions');
  if (liveBadge) liveBadge.textContent = adminState.sessions.length;
  if (statLive) statLive.textContent = adminState.sessions.length;
}

function renderLoginsTable() {
  const tbody = document.getElementById('logins-table-tbody');
  if (!tbody) return;

  const filtered = adminState.logins.filter(l => {
    if (adminState.loginFilter === 'SUCCESS' && l.status !== 'SUCCESS') return false;
    if (adminState.loginFilter === 'FAILED' && l.status === 'SUCCESS') return false;
    if (adminState.loginSearch) {
      const q = adminState.loginSearch;
      if (!l.user.toLowerCase().includes(q) && !l.ip.toLowerCase().includes(q) && !l.details.toLowerCase().includes(q)) {
        return false;
      }
    }
    return true;
  });

  tbody.innerHTML = filtered.map(l => {
    let badgeClass = 'badge-success';
    if (l.status === 'BLOCKED_IP') badgeClass = 'badge-danger';
    if (l.status === 'FAILED_PASSWORD') badgeClass = 'badge-warning';

    return `
      <tr>
        <td><code>${l.time}</code></td>
        <td><strong>${l.user}</strong></td>
        <td><code>${l.ip}</code></td>
        <td><small style="color: var(--text-dim);">${l.agent}</small></td>
        <td><span class="badge ${badgeClass}">${l.status}</span></td>
        <td><small style="color: var(--text-muted);">${l.details}</small></td>
      </tr>
    `;
  }).join('');
}

function renderFullActivitiesTimeline() {
  const container = document.getElementById('full-activities-timeline');
  if (!container) return;

  container.innerHTML = adminState.activities.map(a => `
    <div class="timeline-item">
      <div class="tl-avatar" style="background: ${a.color};">${a.avatar}</div>
      <div class="tl-content">
        <div class="tl-head">
          <span class="tl-text"><strong>${a.actor}</strong> ${a.action} <strong>${a.target}</strong></span>
          <span class="tl-time">${a.time}</span>
        </div>
        <div class="tl-details">${a.details}</div>
      </div>
    </div>
  `).join('');
}

// ==========================================
// USER & SESSION ACTIONS
// ==========================================

window.filterUserTable = function(q) {
  adminState.userSearch = q.toLowerCase();
  renderUsersTable();
};

window.setUserRoleFilter = function(role) {
  adminState.roleFilter = role;
  document.querySelectorAll('#view-users .pill-btn').forEach(btn => {
    btn.classList.toggle('active', btn.textContent.toUpperCase().includes(role) || (role === 'ALL' && btn.textContent.includes('All')));
  });
  renderUsersTable();
};

window.toggleUserStatus = function(id) {
  const user = adminState.users.find(u => u.id === id);
  if (user) {
    user.status = user.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    renderUsersTable();
    showToast(`User ${user.name} is now ${user.status}.`, user.status === 'ACTIVE' ? 'success' : 'danger');
  }
};

window.resetPasswordPrompt = function(email) {
  showToast(`Password reset link dispatched to ${email}.`, 'success');
};

window.revokeSession = function(sessionId) {
  const idx = adminState.sessions.findIndex(s => s.id === sessionId);
  if (idx !== -1) {
    const s = adminState.sessions[idx];
    adminState.sessions.splice(idx, 1);
    renderSessionsTable();
    showToast(`Session ${s.id} (${s.userName}) revoked and token invalidated in Redis.`, 'danger');
  }
};

window.revokeAllSessions = function() {
  if (confirm("Terminate all other active sessions across the organization?")) {
    const keepCurrent = adminState.sessions.filter(s => s.userName === "Chaitanya");
    adminState.sessions = keepCurrent;
    renderSessionsTable();
    showToast("All foreign active sessions terminated.", 'danger');
  }
};

window.filterLoginTable = function(q) {
  adminState.loginSearch = q.toLowerCase();
  renderLoginsTable();
};

window.setLoginStatusFilter = function(filter) {
  adminState.loginFilter = filter;
  document.querySelectorAll('#view-logins .pill-btn').forEach(btn => {
    btn.classList.toggle('active', btn.textContent.toUpperCase().includes(filter) || (filter === 'ALL' && btn.textContent.includes('All')));
  });
  renderLoginsTable();
};

// Modal functions
window.openNewUserModal = function() {
  const modal = document.getElementById('new-user-modal');
  if (modal) modal.classList.add('open');
};

window.closeNewUserModal = function() {
  const modal = document.getElementById('new-user-modal');
  if (modal) modal.classList.remove('open');
};

window.handleCreateUser = function(e) {
  e.preventDefault();
  const name = document.getElementById('nu-name').value;
  const email = document.getElementById('nu-email').value;
  const role = document.getElementById('nu-role').value;

  const newUser = {
    id: "usr-0" + (adminState.users.length + 1),
    name: name,
    email: email,
    role: role,
    mfa: true,
    status: "ACTIVE",
    lastActive: "Just created",
    avatar: name.charAt(0).toUpperCase(),
    color: "linear-gradient(135deg, #3B82F6, #10B981)"
  };

  adminState.users.push(newUser);
  renderUsersTable();
  closeNewUserModal();
  showToast(`User account for ${name} created successfully.`, 'success');
  document.getElementById('create-user-form').reset();
};

// Maintenance Actions
window.runMaintenanceAction = function(action) {
  const log = document.getElementById('maint-log-container');
  const now = new Date().toLocaleTimeString();

  if (action === 'vacuum') {
    showToast("Running VACUUM ANALYZE across PostgreSQL 15...", "success");
    if (log) {
      const entry = document.createElement('div');
      entry.className = 'maint-entry';
      entry.innerHTML = `<span class="maint-time">${now}</span> <span class="maint-text">ADMIN TRIGGERED: VACUUM ANALYZE completed on 5 tables. 0 dead tuples.</span>`;
      log.prepend(entry);
    }
  } else if (action === 'prune') {
    showToast("Pruning expired tokens and sessions older than 30 days...", "success");
    if (log) {
      const entry = document.createElement('div');
      entry.className = 'maint-entry';
      entry.innerHTML = `<span class="maint-time">${now}</span> <span class="maint-text">ADMIN TRIGGERED: Session garbage collection pruned 14 expired records.</span>`;
      log.prepend(entry);
    }
  }
};

// Export Functionality
window.exportAdminData = function(format) {
  let content = "";
  let filename = "";
  let type = "";

  if (format === 'json') {
    content = JSON.stringify({
      exportTimestamp: new Date().toISOString(),
      platform: "InfraPilot Admin & Operations Console",
      users: adminState.users,
      activeSessions: adminState.sessions,
      loginHistory: adminState.logins,
      developerActivities: adminState.activities
    }, null, 2);
    filename = `infrapilot-admin-dump-${Date.now()}.json`;
    type = "application/json";
  } else {
    // CSV Export
    const rows = [
      ["Timestamp", "User Email", "IP Address", "Status", "Details"],
      ...adminState.logins.map(l => [l.time, l.user, l.ip, l.status, l.details])
    ];
    content = rows.map(r => r.map(c => `"${c}"`).join(",")).join("\n");
    filename = `infrapilot-login-audit-${Date.now()}.csv`;
    type = "text/csv";
  }

  const blob = new Blob([content], { type: type });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
  showToast(`Exported ${filename} successfully.`, 'success');
};

// Toast helper
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span>${type === 'success' ? '✓' : '⚠️'}</span><span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}
