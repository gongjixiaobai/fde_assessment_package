// ===== API Helpers =====
const API_BASE = '/api';

async function apiGet(path) {
    const res = await fetch(API_BASE + path);
    if (!res.ok) throw { status: res.status, body: await res.json().catch(() => ({})) };
    return res.json();
}

async function apiPost(path, body) {
    const res = await fetch(API_BASE + path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (!res.ok) throw { status: res.status, body: await res.json().catch(() => ({})) };
    return res.json();
}

async function apiPut(path, body) {
    const res = await fetch(API_BASE + path, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (!res.ok) throw { status: res.status, body: await res.json().catch(() => ({})) };
    return res.json();
}

async function apiDelete(path) {
    const res = await fetch(API_BASE + path, { method: 'DELETE' });
    if (!res.ok) throw { status: res.status, body: await res.json().catch(() => ({})) };
    return true;
}

function showMessage(areaId, type, msg) {
    const area = document.getElementById(areaId);
    if (!area) return;
    area.innerHTML = `<div class="${type === 'error' ? 'error-msg' : 'success-msg'}">${msg}</div>`;
    if (type === 'success') setTimeout(() => area.innerHTML = '', 3000);
}

function getStatusClass(status) {
    const map = {
        'DRAFT': 'badge-draft', 'ANALYZING': 'badge-analyzing', 'READY': 'badge-ready',
        'IN_PROGRESS': 'badge-in_progress', 'TESTING': 'badge-testing', 'DONE': 'badge-done'
    };
    return map[status] || 'badge-default';
}

function getStatusLabel(status) {
    const map = {
        'DRAFT': '草稿', 'ANALYZING': '待分析', 'READY': '已准备',
        'IN_PROGRESS': '开发中', 'TESTING': '测试中', 'DONE': '已完成'
    };
    return map[status] || status;
}

// ===== List Page =====
async function loadWorkItems() {
    const status = document.getElementById('filterStatus').value;
    const type = document.getElementById('filterType').value;
    const priority = document.getElementById('filterPriority').value;
    const keyword = document.getElementById('filterKeyword').value;

    let params = `?page=1&size=50`;
    if (status) params += `&status=${status}`;
    if (type) params += `&type=${type}`;
    if (priority) params += `&priority=${priority}`;
    if (keyword) params += `&keyword=${encodeURIComponent(keyword)}`;

    try {
        const data = await apiGet('/workitems' + params);
        renderTable(data);
    } catch (e) {
        document.getElementById('workItemTable').innerHTML = `<div class="error-msg">加载失败: ${e.body?.message || e.status}</div>`;
    }
}

function renderTable(items) {
    if (!items || items.length === 0) {
        document.getElementById('workItemTable').innerHTML = '<div class="loading">暂无数据</div>';
        return;
    }
    let html = `<table><thead><tr>
        <th>ID</th><th>标题</th><th>类型</th><th>优先级</th><th>状态</th><th>负责人</th><th>更新时间</th><th>操作</th>
    </tr></thead><tbody>`;
    items.forEach(item => {
        html += `<tr>
            <td>${item.id}</td>
            <td><a class="nav-link" href="/detail?id=${item.id}">${escapeHtml(item.title)}</a></td>
            <td><span class="badge">${item.type}</span></td>
            <td><span class="badge badge-${(item.priority||'').toLowerCase()}">${item.priority}</span></td>
            <td><span class="status-badge ${getStatusClass(item.status)}">${getStatusLabel(item.status)}</span></td>
            <td>${item.assignee || '-'}</td>
            <td>${formatDate(item.updatedAt)}</td>
            <td>
                <a href="/detail?id=${item.id}" class="btn btn-default btn-sm">详情</a>
                <button class="btn btn-danger btn-sm" onclick="deleteWorkItem(${item.id})">删除</button>
            </td>
        </tr>`;
    });
    html += '</tbody></table>';
    document.getElementById('workItemTable').innerHTML = html;
}

async function createWorkItem() {
    const body = {
        title: document.getElementById('createTitle').value.trim(),
        description: document.getElementById('createDesc').value.trim(),
        type: document.getElementById('createType').value,
        priority: document.getElementById('createPriority').value,
        assignee: document.getElementById('createAssignee').value.trim(),
        tags: document.getElementById('createTags').value.trim(),
        acceptanceCriteria: document.getElementById('createCriteria').value.trim(),
        riskLevel: document.getElementById('createRisk').value || null
    };
    if (!body.title) {
        showMessage('messageArea', 'error', '标题不能为空');
        return;
    }
    try {
        await apiPost('/workitems', body);
        closeCreateDialog();
        loadWorkItems();
        showMessage('messageArea', 'success', '创建成功');
    } catch (e) {
        showMessage('messageArea', 'error', e.body?.message || '创建失败');
    }
}

async function deleteWorkItem(id) {
    if (!confirm('确认删除工作项 #' + id + '?')) return;
    try {
        await apiDelete(`/workitems/${id}`);
        loadWorkItems();
        showMessage('messageArea', 'success', '删除成功');
    } catch (e) {
        showMessage('messageArea', 'error', e.body?.message || '删除失败');
    }
}

// ===== Detail Page =====
let currentWorkItem = null;

async function loadDetail() {
    try {
        const data = await apiGet(`/workitems/${WORK_ITEM_ID}`);
        currentWorkItem = data;
        renderInfo(data);
        renderTransitions(data);
        renderQuestions(data.questions);
        renderHistory(data.transitions);
        document.getElementById('transitionCard').style.display = 'block';
        document.getElementById('questionCard').style.display = 'block';
        document.getElementById('aiCard').style.display = 'block';
        document.getElementById('historyCard').style.display = 'block';
        loadAllowedTransitions();
    } catch (e) {
        document.getElementById('infoCard').innerHTML = `<div class="error-msg">加载失败: ${e.body?.message || e.status}</div>`;
    }
}

function renderInfo(item) {
    document.getElementById('infoCard').innerHTML = `
        <div class="card-title">${escapeHtml(item.title)} <span class="status-badge ${getStatusClass(item.status)}">${getStatusLabel(item.status)}</span></div>
        <div class="flex-row">
            <div class="form-group flex-grow"><label>类型</label><div>${item.type}</div></div>
            <div class="form-group flex-grow"><label>优先级</label><div><span class="badge badge-${(item.priority||'').toLowerCase()}">${item.priority}</span></div></div>
            <div class="form-group flex-grow"><label>负责人</label><div>${item.assignee || '-'}</div></div>
        </div>
        <div class="form-group"><label>描述</label><div>${item.description || '无'}</div></div>
        <div class="form-group"><label>标签</label><div>${item.tags || '无'}</div></div>
        <div class="form-group"><label>验收标准</label><div>${item.acceptanceCriteria || '无'}</div></div>
        <div class="form-group"><label>风险等级</label><div>${item.riskLevel || '未设置'}</div></div>
        <div class="form-group"><label>创建时间</label><div>${formatDate(item.createdAt)}</div></div>
    `;
}

function renderTransitions(item) {
    const status = item.status;
    document.getElementById('transitionContent').innerHTML = `
        <div class="mb-8">当前状态: <span class="status-badge ${getStatusClass(status)}">${getStatusLabel(status)}</span></div>
        <div id="transitionButtons" class="mb-8">加载中...</div>
        <div id="transitionError"></div>
    `;
}

async function loadAllowedTransitions() {
    try {
        const data = await apiGet(`/workitems/${WORK_ITEM_ID}/ai-analysis/allowed`);
        const allowed = data.allowedTransitions || [];
        let html = '';
        allowed.forEach(s => {
            html += `<button class="btn btn-primary btn-sm" onclick="doTransition('${s}')" style="margin-right:8px;">→ ${getStatusLabel(s)}</button>`;
        });
        if (!allowed.length) html = '<span style="color:#999;">当前状态不允许流转</span>';
        document.getElementById('transitionButtons').innerHTML = html;
    } catch (e) {
        document.getElementById('transitionButtons').innerHTML = '无法加载';
    }
}

async function doTransition(target) {
    try {
        await apiPost(`/workitems/${WORK_ITEM_ID}/transitions`, { toStatus: target, operator: 'user' });
        showMessage('messageArea', 'success', `状态流转至「${getStatusLabel(target)}」成功`);
        loadDetail();
    } catch (e) {
        const msg = e.body?.message || '状态流转失败';
        document.getElementById('transitionError').innerHTML = `<div class="error-msg">${msg}</div>`;
    }
}

function renderQuestions(questions) {
    let html = '';
    if (questions && questions.length > 0) {
        questions.forEach(q => {
            html += `<div class="question-item">
                <div class="q-header">
                    <span class="badge badge-${(q.severity||'').toLowerCase()}">${q.severity}</span>
                    <span class="badge ${q.status === 'RESOLVED' ? 'badge-done' : 'badge-analyzing'}">${q.status === 'RESOLVED' ? '已解决' : '未解决'}</span>
                </div>
                <div class="q-content">${escapeHtml(q.content)}</div>
                ${q.answer ? `<div class="q-answer"><strong>回答:</strong> ${escapeHtml(q.answer)}</div>` : ''}
                ${q.status === 'UNRESOLVED' ? `<button class="btn btn-success btn-sm mt-8" onclick="showResolveDialog(${q.id})">回答</button>` : ''}
            </div>`;
        });
    } else {
        html = '<div style="color:#999;">暂无澄清问题</div>';
    }
    document.getElementById('questionList').innerHTML = html;
}

function showResolveDialog(qid) {
    const answer = prompt('输入该问题的回答:');
    if (!answer) return;
    resolveQuestion(qid, answer);
}

async function resolveQuestion(qid, answer) {
    try {
        await apiPut(`/workitems/${WORK_ITEM_ID}/questions/${qid}`, { answer });
        loadDetail();
        showMessage('messageArea', 'success', '问题已解决');
    } catch (e) {
        showMessage('messageArea', 'error', e.body?.message || '操作失败');
    }
}

async function addQuestion() {
    const content = document.getElementById('newQuestionContent').value.trim();
    const severity = document.getElementById('newQuestionSeverity').value;
    if (!content) return;
    try {
        await apiPost(`/workitems/${WORK_ITEM_ID}/questions`, { content, severity });
        document.getElementById('newQuestionContent').value = '';
        loadDetail();
        showMessage('messageArea', 'success', '澄清问题已添加');
    } catch (e) {
        showMessage('messageArea', 'error', e.body?.message || '添加失败');
    }
}

async function triggerAIAnalysis() {
    const btn = document.getElementById('aiBtn');
    btn.disabled = true;
    btn.textContent = '分析中...';
    try {
        const result = await apiPost(`/workitems/${WORK_ITEM_ID}/ai-analysis`, {});
        renderAIResult(result);
    } catch (e) {
        document.getElementById('aiResult').innerHTML = `<div class="error-msg">AI 分析失败: ${e.body?.message || e.status}</div>`;
    } finally {
        btn.disabled = false;
        btn.textContent = '触�� AI 分析';
    }
}

function renderAIResult(result) {
    let html = '<div class="ai-result">';
    if (result.summary) {
        html += `<h4>📝 需求摘要</h4><p style="font-size:14px;">${escapeHtml(result.summary)}</p>`;
    }
    if (result.risks && result.risks.length > 0) {
        html += '<h4>⚠️ 风险点</h4>';
        result.risks.forEach(r => {
            html += `<div class="risk-item risk-${(r.severity||'').toLowerCase()}"><strong>${escapeHtml(r.type)}</strong>: ${escapeHtml(r.description)} (${r.severity})</div>`;
        });
    }
    if (result.acceptanceCriteria && result.acceptanceCriteria.length > 0) {
        html += '<h4>✅ 建议验收标准</h4><ul>';
        result.acceptanceCriteria.forEach(c => html += `<li>${escapeHtml(c)}</li>`);
        html += '</ul>';
    }
    if (result.clarificationQuestions && result.clarificationQuestions.length > 0) {
        html += '<h4>❓ 建议澄清问题</h4><ul>';
        result.clarificationQuestions.forEach(q => html += `<li>[${q.severity}] ${escapeHtml(q.question)}</li>`);
        html += '</ul>';
    }
    if (result.taskSuggestions && result.taskSuggestions.length > 0) {
        html += '<h4>📋 任务拆解建议</h4><ul>';
        result.taskSuggestions.forEach(t => html += `<li>${escapeHtml(t)}</li>`);
        html += '</ul>';
    }
    html += '</div>';
    document.getElementById('aiResult').innerHTML = html;
}

function renderHistory(transitions) {
    let html = '';
    if (transitions && transitions.length > 0) {
        html += '<ul class="transition-list">';
        transitions.forEach(t => {
            html += `<li>[${formatDate(t.createdAt)}] ${t.fromStatus || '(初始)'} <span class="transition-arrow">→</span> ${t.toStatus} (操作人: ${t.operator || '-'})</li>`;
        });
        html += '</ul>';
    } else {
        html = '<div style="color:#999;">暂无流转记录</div>';
    }
    document.getElementById('historyList').innerHTML = html;
}

// ===== Helpers =====
function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    try {
        return new Date(dateStr).toLocaleString('zh-CN');
    } catch (e) {
        return dateStr;
    }
}
