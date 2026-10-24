let tasks = [];

document.addEventListener('DOMContentLoaded', () => {
    fetchTasks();
});

async function fetchTasks() {
    try {
        const response = await fetch('/api/tasks');
        if (response.ok) {
            tasks = await response.json();
            renderTasks(tasks);
            updateStats(tasks);
        }
    } catch (error) {
        console.error('Failed to load tasks:', error);
    }
}

function updateStats(items) {
    document.getElementById('statTotal').innerText = items.length;
    document.getElementById('statInProgress').innerText = items.filter(t => t.status === 'IN_PROGRESS').length;
    document.getElementById('statDone').innerText = items.filter(t => t.status === 'DONE').length;
    document.getElementById('statHigh').innerText = items.filter(t => t.priority === 'HIGH').length;
}

function renderTasks(items) {
    const grid = document.getElementById('tasksGrid');
    if (!items || items.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: var(--text-secondary); padding: 3rem;">No tasks found. Click "+ Create Task" to get started!</div>`;
        return;
    }

    grid.innerHTML = items.map(task => `
        <div class="task-card">
            <div>
                <div class="task-header">
                    <span class="task-title">${escapeHtml(task.title)}</span>
                    <span class="badge badge-${task.status.toLowerCase()}">${task.status.replace('_', ' ')}</span>
                </div>
                <div class="task-desc">${escapeHtml(task.description || 'No description provided.')}</div>
            </div>
            <div class="task-footer">
                <span class="priority-${task.priority.toLowerCase()}">Priority: ${task.priority}</span>
                <div class="task-actions">
                    <button class="btn-icon" onclick="openEditModal(${task.id})" title="Edit">✏️</button>
                    <button class="btn-icon" onclick="deleteTask(${task.id})" title="Delete">🗑️</button>
                </div>
            </div>
        </div>
    `).join('');
}

function filterTasks() {
    const val = document.getElementById('statusFilter').value;
    if (!val) {
        renderTasks(tasks);
    } else {
        renderTasks(tasks.filter(t => t.status === val));
    }
}

function openCreateModal() {
    document.getElementById('taskId').value = '';
    document.getElementById('taskTitleInput').value = '';
    document.getElementById('taskDescInput').value = '';
    document.getElementById('taskStatusInput').value = 'TODO';
    document.getElementById('taskPriorityInput').value = 'MEDIUM';
    document.getElementById('modalTitle').innerText = 'Create New Task';
    document.getElementById('taskModal').classList.add('active');
}

function openEditModal(id) {
    const task = tasks.find(t => t.id === id);
    if (!task) return;
    document.getElementById('taskId').value = task.id;
    document.getElementById('taskTitleInput').value = task.title;
    document.getElementById('taskDescInput').value = task.description || '';
    document.getElementById('taskStatusInput').value = task.status;
    document.getElementById('taskPriorityInput').value = task.priority;
    document.getElementById('modalTitle').innerText = 'Edit Task';
    document.getElementById('taskModal').classList.add('active');
}

function closeModal() {
    document.getElementById('taskModal').classList.remove('active');
}

async function handleTaskSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('taskId').value;
    const taskPayload = {
        title: document.getElementById('taskTitleInput').value.trim(),
        description: document.getElementById('taskDescInput').value.trim(),
        status: document.getElementById('taskStatusInput').value,
        priority: document.getElementById('taskPriorityInput').value
    };

    try {
        let res;
        if (id) {
            res = await fetch(`/api/tasks/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(taskPayload)
            });
        } else {
            res = await fetch('/api/tasks', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(taskPayload)
            });
        }

        if (res.ok) {
            closeModal();
            fetchTasks();
        } else {
            alert('Error saving task');
        }
    } catch (err) {
        alert('Failed: ' + err.message);
    }
}

async function deleteTask(id) {
    if (!confirm('Are you sure you want to delete this task?')) return;
    try {
        const res = await fetch(`/api/tasks/${id}`, { method: 'DELETE' });
        if (res.ok) {
            fetchTasks();
        }
    } catch (err) {
        alert('Failed to delete task: ' + err.message);
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
