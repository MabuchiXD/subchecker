import * as api from './api.js?v=1.9';
import { openEditModal } from './subModal.js?v=1.9';

let selectedUser = null;

export function openUserModal(user) {
    selectedUser = user;

    const userId = user.telegramId || user.id;

    const name = user.firstName ? user.firstName : "Без имени";
    document.getElementById('user-modal-title').innerText = `👤 Профиль: ${name}`;
    document.getElementById('user-detail-id').innerText = userId;
    document.getElementById('user-detail-username').innerText = user.username ? `@${user.username}` : "нет";
    document.getElementById('user-detail-name').innerText = name;
    document.getElementById('user-detail-role').innerText = user.role;
    document.getElementById('user-detail-time').innerText = user.preferredNotificationTime || "10:00";
    document.getElementById('user-detail-timezone').innerText = `UTC ${user.timezoneOffset >= 0 ? '+' : ''}${user.timezoneOffset}`;
    document.getElementById('user-detail-currency').innerText = user.defaultCurrency || "RUB";

    document.getElementById('user-detail-subs-list').innerText = "Загрузка подписок...";

    api.fetchUserSubs(userId)
        .then(subs => renderUserSubsInModal(subs))
        .catch((err) => {
            // 🔴 ВЫВОДИМ КОНКРЕТНЫЙ КОД ОШИБКИ НА ЭКРАН ТЕЛЕФОНА
            document.getElementById('user-detail-subs-list').innerText = `❌ Ошибка: ${err.message || err}`;
        });

    const roleBtn = document.getElementById('btn-user-toggle-role');
    if (user.role === "ADMIN") {
        roleBtn.innerText = "👤 Понизить до Пользователя";
        roleBtn.style.backgroundColor = "var(--danger-color)";
    } else {
        roleBtn.innerText = "👑 Повысить до Администратора";
        roleBtn.style.backgroundColor = "var(--accent-color)";
    }

    document.getElementById('user-modal-overlay').classList.add('active');
}

export function closeUserModal() {
    document.getElementById('user-modal-overlay').classList.remove('active');
    selectedUser = null;
}

function renderUserSubsInModal(subs) {
    const container = document.getElementById('user-detail-subs-list');
    if (!container) return;
    container.innerHTML = '';

    if (subs.length === 0) {
        container.innerHTML = '<div style="color: var(--hint-color); font-size: 13px; text-align: center; padding: 10px 0;">Нет активных подписок</div>';
        return;
    }

    subs.forEach(sub => {
        const item = document.createElement('div');
        item.className = 'mini-sub-card';
        item.innerHTML = `
            <span><b>${sub.serviceName.toUpperCase()}</b></span>
            <span class="price">${sub.price} ${sub.currency}</span>
        `;

        item.addEventListener('click', () => {
            closeUserModal();
            openEditModal(sub);
        });

        container.appendChild(item);
    });
}

export function setupUserModalListeners(onRefresh) {
    document.getElementById('btn-close-user-modal').addEventListener('click', closeUserModal);
    document.getElementById('user-modal-overlay').addEventListener('click', (e) => {
        if (e.target === document.getElementById('user-modal-overlay')) closeUserModal();
    });

    document.getElementById('btn-user-toggle-role').addEventListener('click', () => {
        if (!selectedUser) return;

        const userId = selectedUser.telegramId || selectedUser.id;
        const newRole = selectedUser.role === "ADMIN" ? "USER" : "ADMIN";

        api.changeUserRole(userId, newRole)
            .then(res => {
                if (!res.ok) throw new Error();
                alert(`✅ Роль изменена на ${newRole}!`);
                closeUserModal();
                onRefresh();
            })
            .catch(() => alert("❌ Ошибка изменения роли."));
    });

    document.getElementById('btn-user-reset-session').addEventListener('click', () => {
        if (!selectedUser) return;

        const userId = selectedUser.telegramId || selectedUser.id;

        if (confirm(`Сбросить сессию для ${selectedUser.firstName || selectedUser.username}?`)) {
            api.resetUserSession(userId)
                .then(res => {
                    if (!res.ok) throw new Error();
                    alert("✅ Сессия пользователя сброшена в IDLE!");
                    closeUserModal();
                })
                .catch(() => alert("❌ Не удалось сбросить сессию."));
        }
    });
}