const tg = window.Telegram && window.Telegram.WebApp ? window.Telegram.WebApp : null;

function getAuthHeader() {
    return tg && tg.initData && tg.initData !== '' ? tg.initData : "MOCK_DATA";
}

function getSessionToken() {
    return localStorage.getItem('sessionToken');
}

// 🔴 ОБНОВЛЕНО: Теперь выбрасывает ошибку с кодом (403, 500 и т.д.), если сервер ответил неудачно
function request(url, options = {}) {
    options.headers = options.headers || {};
    options.headers['X-Telegram-Init-Data'] = getAuthHeader();

    const token = getSessionToken();
    if (token) {
        options.headers['X-Session-Token'] = token;
    }

    return fetch(url, options).then(res => {
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`); // Выбрасываем код ошибки
        }
        return res;
    });
}

export function fetchMe() {
    return request('/api/web/users/me').then(res => res.json());
}

export function login(code) {
    return fetch(`/api/web/users/login?code=${code}`, {
        method: 'POST'
    }).then(res => {
        if (!res.ok) return res.json().then(data => { throw new Error(data.error); });
        return res.json();
    });
}

export function fetchUserRole() {
    return request('/api/web/users/role').then(res => res.json());
}

export function fetchUserSettings() {
    return request('/api/web/users/settings').then(res => res.json());
}

export function saveUserSettings(data) {
    return request('/api/web/users/settings', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
}

export function fetchUserSubs(tgId) {
    const path = tgId ? `/api/web/subscriptions/user/${tgId}` : '/api/web/subscriptions';
    return request(path).then(res => res.json());
}

export function joinByInviteCode(code) {
    return request(`/api/web/subscriptions/join?code=${code}`, {
        method: 'POST'
    }).then(res => {
        if (!res.ok) return res.json().then(data => { throw new Error(data.error); });
        return res.json();
    });
}

export function createSubscription(data) {
    return request('/api/web/subscriptions/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
}

export function saveSubscriptionEdit(subId, data) {
    return request(`/api/web/subscriptions/${subId}/edit`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
}

export function renewSubscription(subId, period) {
    return request(`/api/web/subscriptions/${subId}/renew?days=${period}`, {
        method: 'POST'
    });
}

export function generateInviteCode(subId) {
    return request(`/api/web/subscriptions/${subId}/invite`, {
        method: 'POST'
    }).then(res => res.json());
}

export function deleteSubscription(subId) {
    return request(`/api/web/subscriptions/${subId}`, {
        method: 'DELETE'
    });
}

export function fetchAdminStats() {
    return request('/api/web/admin/stats').then(res => res.json());
}

export function fetchAdminAllUsers() {
    return request('/api/web/admin/users').then(res => res.json());
}

export function changeUserRole(userId, newRole) {
    return request(`/api/web/admin/users/${userId}/role?role=${newRole}`, {
        method: 'PUT'
    });
}

export function resetUserSession(userId) {
    return request(`/api/web/admin/users/${userId}/reset`, {
        method: 'POST'
    });
}
export function logoutAllDevices() {
    return fetch('/api/web/users/logout-all', {
        method: 'POST',
        headers: {
            'X-Session-Token': localStorage.getItem('sessionToken')
        }
    }).then(res => {
        if (!res.ok) throw new Error("Не удалось выполнить сброс сессий");
        return res;
    });
}
export function revokeInviteCode(subId) {
    return request(`/api/web/subscriptions/${subId}/revoke-invite`, {
        method: 'POST'
    });
}