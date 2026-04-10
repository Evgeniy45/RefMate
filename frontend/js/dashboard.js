let currentUser = null;
let newSelectedRefereesList = [];
let editSelectedRefereesList = [];

function getAuthHeaders() {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = 'index.html';
        return {};
    }
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

document.addEventListener('DOMContentLoaded', () => {
    const userData = localStorage.getItem('currentUser');
    const token = localStorage.getItem('jwtToken');
    
    if (!userData || !token) { window.location.href = 'index.html'; return; }
    currentUser = JSON.parse(userData);

    document.getElementById('userName').textContent = currentUser.fullName;
    document.getElementById('userRole').textContent = currentUser.role === 'ADMIN' ? 'Головний суддя' : 'Арбітр';

    if (currentUser.role === 'ADMIN') {
        document.getElementById('adminPanel').style.display = 'block';
        document.getElementById('refereePanel').style.display = 'block'; 
        loadReferees(); 
        loadMatches(); 
    } else {
        document.getElementById('refereePanel').style.display = 'block';
    }
    loadMyMatches();
    renderMyDates();

    document.getElementById('logoutBtn').addEventListener('click', logout);
    attachAdminEventListeners();
    attachRefereeEventListeners();
});

function logout() {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('jwtToken'); 
    window.location.href = 'index.html';
}

async function loadReferees() {
    try {
        const response = await fetch('https://refmate-api.onrender.com/api/users', { headers: getAuthHeaders() });
        if (!response.ok) {
            if (response.status === 401) logout(); 
            return;
        }
        const users = await response.json();
        const tbody = document.querySelector('#refereesTable tbody');
        if (!tbody) return; tbody.innerHTML = '';
        
        users.forEach(ref => {
            const isMe = ref.id === currentUser.id;
            const roleBadge = ref.role === 'ADMIN' ? '<span style="color:red; font-size:10px;">(Головний)</span>' : '';
            const deleteBtn = isMe ? '' : `<button onclick="deleteUser(${ref.id})" class="button button--small" style="background:#e63946; padding:2px 6px;">🗑️</button>`;

            tbody.innerHTML += `
                <tr>
                    <td>${ref.fullName} ${roleBadge}</td>
                    <td>${ref.city}</td>
                    <td><span class="badge">${ref.licenseCategory}</span></td>
                    <td>${ref.email}</td>
                    <td>${deleteBtn}</td>
                </tr>`;
        });
    } catch (error) { console.error(error); }
}

async function deleteUser(id) {
    const result = await Swal.fire({
        title: 'Ви впевнені?',
        text: "Цього арбітра буде назавжди видалено з системи!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#e63946',
        cancelButtonColor: '#aaa',
        confirmButtonText: 'Так, видалити',
        cancelButtonText: 'Скасувати'
    });

    if (result.isConfirmed) {
        try {
            const response = await fetch(`https://refmate-api.onrender.com/api/users/${id}`, { 
                method: 'DELETE',
                headers: getAuthHeaders()
            });
            if (response.ok) {
                Swal.fire('Видалено!', 'Арбітра успішно видалено.', 'success');
                loadReferees();
            }
        } catch (error) { console.error(error); }
    }
}

async function loadMatches() {
    try {
        const response = await fetch('https://refmate-api.onrender.com/api/matches', { headers: getAuthHeaders() });
        const matches = await response.json();
        const tbody = document.querySelector('#matchesTable tbody');
        if (!tbody) return; tbody.innerHTML = '';

        matches.forEach(match => {
            const dateObj = new Date(match.dateTime);
            const formattedDate = dateObj.toLocaleString('uk-UA', { dateStyle: 'short', timeStyle: 'short' });
            
            let statusesArray = [];
            let refsHtml = match.referees?.map(r => {
                const stat = match.refereeStatuses ? match.refereeStatuses[r.id] : 'PENDING';
                statusesArray.push(stat);
                let icon = stat === 'ACCEPTED' ? '🟢' : (stat === 'DECLINED' ? '🔴' : '🟡');
                return `<div style="margin-bottom: 2px;">${icon} ${r.fullName}</div>`;
            }).join('') || 'Не призначено';

            let globalStatusBadge = '';
            let actionButtons = '';

            if (match.finished) {
                globalStatusBadge = `<span class="badge" style="background:#333; color:white;">🏁 Завершено</span>`;
                actionButtons = `<button onclick="deleteMatch(${match.id})" class="button button--small" style="background:#e63946;">🗑️</button>`;
            } else {
                if (statusesArray.includes('DECLINED')) globalStatusBadge = '<span class="badge" style="background:#e63946; color:white;">🔴 Відхилено</span>';
                else if (statusesArray.every(s => s === 'ACCEPTED') && statusesArray.length > 0) globalStatusBadge = '<span class="badge" style="background:#2a9d8f; color:white;">🟢 Підтверджено</span>';
                else globalStatusBadge = '<span class="badge" style="background:#f4a261; color:white;">🟡 Очікується</span>';

                actionButtons = `
                    <button onclick="openEditModal(${match.id})" class="button button--small" style="background:#f4a261;">✏️</button>
                    <button onclick="finishMatch(${match.id})" class="button button--small" style="background:#333;" title="Завершити матч">🏁</button>
                    <button onclick="deleteMatch(${match.id})" class="button button--small" style="background:#e63946;">🗑️</button>
                `;
            }

            tbody.innerHTML += `
                <tr>
                    <td>${formattedDate}</td>
                    <td><strong>${match.teamA} - ${match.teamB}</strong></td>
                    <td>${match.location}</td>
                    <td><div style="font-size:13px; background:#f0f0f0; padding:5px; border-radius:4px;">${refsHtml}</div></td>
                    <td>${globalStatusBadge}</td>
                    <td style="display:flex; gap:4px;">${actionButtons}</td>
                </tr>`;
        });
    } catch (error) { console.error(error); }
}

async function finishMatch(id) {
    const result = await Swal.fire({
        title: 'Завершити матч?',
        text: "Після цього арбітри більше не зможуть змінити своє рішення.",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#333',
        cancelButtonColor: '#aaa',
        confirmButtonText: 'Так, завершити',
        cancelButtonText: 'Скасувати'
    });

    if (result.isConfirmed) {
        try {
            const response = await fetch(`https://refmate-api.onrender.com/api/matches/${id}/finish`, { 
                method: 'PUT',
                headers: getAuthHeaders()
            });
            if (response.ok) loadMatches();
        } catch (error) { console.error(error); }
    }
}

async function deleteMatch(id) {
    const result = await Swal.fire({
        title: 'Видалити матч?',
        text: "Цю дію неможливо скасувати!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#e63946',
        cancelButtonColor: '#aaa',
        confirmButtonText: 'Так, видалити',
        cancelButtonText: 'Скасувати'
    });

    if (result.isConfirmed) {
        await fetch(`https://refmate-api.onrender.com/api/matches/${id}`, { 
            method: 'DELETE',
            headers: getAuthHeaders()
        }); 
        loadMatches(); 
        loadMyMatches(); 
        Swal.fire('Видалено!', 'Матч успішно видалено.', 'success');
    }
}

function attachAdminEventListeners() {
    const findRefereesBtn = document.getElementById('findRefereesBtn');
    const matchRefereeSelect = document.getElementById('matchReferee');
    const matchForm = document.getElementById('matchForm');

    findRefereesBtn?.addEventListener('click', async () => {
        const matchDateTime = document.getElementById('matchDateTime').value;
        if (!matchDateTime) {
            Swal.fire('Увага!', 'Оберіть дату та час матчу!', 'info');
            return;
        }
        
        Swal.fire({ title: 'Шукаємо вільних суддів...', allowOutsideClick: false, didOpen: () => { Swal.showLoading() } });

        try {
            const response = await fetch(`https://refmate-api.onrender.com/api/users/available?date=${matchDateTime.split('T')[0]}`, {
                headers: getAuthHeaders()
            });
            const availableReferees = await response.json();
            
            Swal.close(); 

            if(availableReferees.length === 0) {
                Swal.fire('На жаль', 'На цю дату немає вільних суддів.', 'warning');
                return;
            }

            newSelectedRefereesList = []; renderNewSelectedRefereesUI();
            matchRefereeSelect.innerHTML = '<option value="" disabled selected>Оберіть суддю...</option>';
            availableReferees.forEach(ref => {
                matchRefereeSelect.innerHTML += `<option value="${ref.id}">${ref.fullName} - ${ref.licenseCategory}</option>`;
            });
            document.getElementById('refereeSelectGroup').style.display = 'block';
        } catch (e) { console.error(e); Swal.close(); }
    });

    matchRefereeSelect?.addEventListener('change', (e) => {
        const id = parseInt(e.target.value);
        const name = e.target.options[e.target.selectedIndex].text.split(' - ')[0];
        if (!newSelectedRefereesList.find(r => r.id === id)) {
            newSelectedRefereesList.push({ id, name });
            renderNewSelectedRefereesUI();
        }
        e.target.value = "";
    });

    matchForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        if(newSelectedRefereesList.length === 0) {
            Swal.fire('Помилка', 'Оберіть хоча б одного арбітра для матчу!', 'error');
            return;
        }

        const newMatch = {
            teamA: document.getElementById('teamA').value, teamB: document.getElementById('teamB').value,
            location: document.getElementById('matchLocation').value, dateTime: document.getElementById('matchDateTime').value,
            referees: newSelectedRefereesList.map(r => ({ id: r.id }))
        };
        
        Swal.fire({ title: 'Створення матчу...', text: 'Розсилаємо листи арбітрам...', allowOutsideClick: false, didOpen: () => { Swal.showLoading() } });

        const response = await fetch('https://refmate-api.onrender.com/api/matches', {
            method: 'POST', 
            headers: getAuthHeaders(), 
            body: JSON.stringify(newMatch)
        });
        
        if (response.ok) { 
            matchForm.reset(); 
            newSelectedRefereesList = []; 
            renderNewSelectedRefereesUI(); 
            document.getElementById('refereeSelectGroup').style.display = 'none';
            loadMatches(); 
            Swal.fire('Успіх!', 'Матч створено. Листи відправлено!', 'success');
        }
    });

    document.getElementById('editMatchReferee')?.addEventListener('change', (e) => {
        const id = parseInt(e.target.value);
        const name = e.target.options[e.target.selectedIndex].text.split(' - ')[0];
        if (!editSelectedRefereesList.find(r => r.id === id)) {
            editSelectedRefereesList.push({ id, name });
            renderEditSelectedRefereesUI();
        }
        e.target.value = "";
    });

    document.getElementById('editMatchForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('editMatchId').value;
        const updated = {
            teamA: document.getElementById('editTeamA').value, teamB: document.getElementById('editTeamB').value,
            location: document.getElementById('editLocation').value, dateTime: document.getElementById('editDateTime').value,
            referees: editSelectedRefereesList.map(r => ({ id: r.id }))
        };
        const res = await fetch(`https://refmate-api.onrender.com/api/matches/${id}`, {
            method: 'PUT', 
            headers: getAuthHeaders(), 
            body: JSON.stringify(updated)
        });
        if (res.ok) { 
            closeEditModal(); 
            loadMatches(); 
            loadMyMatches(); 
            Swal.fire('Оновлено!', 'Дані матчу змінено.', 'success');
        }
    });
}

function renderNewSelectedRefereesUI() {
    const container = document.getElementById('selectedRefereesContainer');
    container.innerHTML = '';
    newSelectedRefereesList.forEach(ref => {
        const badge = document.createElement('span');
        badge.className = 'badge'; badge.style.cssText = 'background:#2a9d8f; color:white; padding:8px 12px; display:inline-flex; align-items:center; gap:8px;';
        badge.innerHTML = `${ref.name} <span style="cursor:pointer;">&times;</span>`;
        badge.querySelector('span').addEventListener('click', () => { newSelectedRefereesList = newSelectedRefereesList.filter(r => r.id !== ref.id); renderNewSelectedRefereesUI(); });
        container.appendChild(badge);
    });
}

async function openEditModal(id) {
    const res = await fetch(`https://refmate-api.onrender.com/api/matches/${id}`, { headers: getAuthHeaders() });
    const match = await res.json();
    document.getElementById('editMatchId').value = match.id;
    document.getElementById('editTeamA').value = match.teamA;
    document.getElementById('editTeamB').value = match.teamB;
    document.getElementById('editLocation').value = match.location;
    document.getElementById('editDateTime').value = match.dateTime;
    editSelectedRefereesList = match.referees.map(r => ({ id: r.id, name: r.fullName }));
    renderEditSelectedRefereesUI();
    loadAvailableRefereesForEdit(match.dateTime.split('T')[0]);
    document.getElementById('editMatchModal').style.display = 'flex';
}

function closeEditModal() { document.getElementById('editMatchModal').style.display = 'none'; }

function renderEditSelectedRefereesUI() {
    const container = document.getElementById('editSelectedRefereesContainer');
    container.innerHTML = '';
    editSelectedRefereesList.forEach(ref => {
        const badge = document.createElement('span');
        badge.className = 'badge'; badge.style.cssText = 'background:#2a9d8f; color:white; padding:8px 12px; display:inline-flex; align-items:center; gap:8px;';
        badge.innerHTML = `${ref.name} <span style="cursor:pointer;">&times;</span>`;
        badge.querySelector('span').addEventListener('click', () => { editSelectedRefereesList = editSelectedRefereesList.filter(r => r.id !== ref.id); renderEditSelectedRefereesUI(); });
        container.appendChild(badge);
    });
}

async function loadAvailableRefereesForEdit(date) {
    const res = await fetch(`https://refmate-api.onrender.com/api/users/available?date=${date}`, { headers: getAuthHeaders() });
    const refs = await res.json();
    const select = document.getElementById('editMatchReferee');
    select.innerHTML = '<option value="" disabled selected>Оберіть суддю...</option>';
    refs.forEach(ref => { select.innerHTML += `<option value="${ref.id}">${ref.fullName} - ${ref.licenseCategory}</option>`; });
}

function attachRefereeEventListeners() {
    document.getElementById('addDateBtn')?.addEventListener('click', () => {
        const input = document.getElementById('availableDate');
        let dates = currentUser.availability ? currentUser.availability.split(',').map(d => d.trim()).filter(d => d) : [];
        
        if (!input.value) {
            Swal.fire('Увага', 'Будь ласка, оберіть дату', 'info');
            return;
        }
        if (dates.includes(input.value)) {
            Swal.fire('Увага', 'Ви вже додали цю дату', 'info');
            return;
        }

        dates.push(input.value); dates.sort();
        saveDatesToBackend(dates); input.value = '';
    });
}

function renderMyDates() {
    const container = document.getElementById('myDatesList');
    if (!container) return; container.innerHTML = '';
    let dates = currentUser.availability ? currentUser.availability.split(',').map(d => d.trim()).filter(d => d) : [];
    dates.forEach(date => {
        const b = document.createElement('span');
        b.className = 'badge'; b.style.cssText = 'background:#e85d04; color:white; padding:8px 12px;';
        b.innerHTML = `${date} <span style="cursor:pointer; margin-left:8px;">&times;</span>`;
        b.querySelector('span').addEventListener('click', () => { dates = dates.filter(d => d !== date); saveDatesToBackend(dates); });
        container.appendChild(b);
    });
}

async function saveDatesToBackend(arr) {
    const res = await fetch(`https://refmate-api.onrender.com/api/users/${currentUser.id}`, {
        method: 'PUT', 
        headers: getAuthHeaders(), 
        body: JSON.stringify({ availability: arr.join(', ') })
    });
    if (res.ok) { 
        currentUser = await res.json(); 
        localStorage.setItem('currentUser', JSON.stringify(currentUser)); 
        renderMyDates(); 
    }
}

async function loadMyMatches() {
    const res = await fetch('https://refmate-api.onrender.com/api/matches', { headers: getAuthHeaders() });
    const all = await res.json();
    const tbody = document.querySelector('#myMatchesTable tbody');
    if (!tbody) return; tbody.innerHTML = '';
    const my = all.filter(m => m.referees?.some(r => r.id === currentUser.id));
    if (!my.length) { tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">Призначень немає</td></tr>'; return; }
    my.forEach(m => {
        const myStat = m.refereeStatuses ? m.refereeStatuses[currentUser.id] : 'PENDING';
        let action = '';
        if (m.finished) action = `<span class="badge" style="background:#333; color:white;">🏁 Завершено</span>`;
        else if (myStat === 'PENDING') action = `<button onclick="changeMatchStatus(${m.id}, ${currentUser.id}, 'ACCEPTED')" class="button button--small" style="background:#2a9d8f;">✅ Підтвердити</button> <button onclick="changeMatchStatus(${m.id}, ${currentUser.id}, 'DECLINED')" class="button button--small" style="background:#e63946;">❌ Відхилити</button>`;
        else action = myStat === 'ACCEPTED' ? '🟢 Підтверджено' : '🔴 Відхилено';
        
        tbody.innerHTML += `<tr><td>${new Date(m.dateTime).toLocaleString('uk-UA', { dateStyle: 'short', timeStyle: 'short' })}</td><td>${m.teamA} - ${m.teamB}</td><td>${m.location}</td><td style="display:flex; gap:5px;">${action}</td></tr>`;
    });
}

async function changeMatchStatus(mid, rid, stat) {
    const actionText = stat === 'ACCEPTED' ? 'підтвердити' : 'відхилити';
    const result = await Swal.fire({
        title: 'Ви впевнені?',
        text: `Ви збираєтесь ${actionText} призначення на цей матч.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: stat === 'ACCEPTED' ? '#2a9d8f' : '#e63946',
        cancelButtonColor: '#aaa',
        confirmButtonText: 'Так',
        cancelButtonText: 'Скасувати'
    });

    if (result.isConfirmed) {
        const res = await fetch(`https://refmate-api.onrender.com/api/matches/${mid}/status?refereeId=${rid}&status=${stat}`, { 
            method: 'PUT',
            headers: getAuthHeaders()
        });
        if (res.ok) { 
            if (currentUser.role === 'ADMIN') loadMatches(); 
            loadMyMatches(); 
            Swal.fire('Збережено!', 'Ваш статус оновлено.', 'success');
        }
    }
}