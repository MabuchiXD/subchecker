import { openEditModal } from './subModal.js?v=1.9';
import { openUserModal } from './userModal.js?v=1.9';

// 1. Отрисовка личных подписок текущего юзера
export function renderSubscriptions(subs, containerId) {
    const listContainer = document.getElementById(containerId);
    if (!listContainer) return;
    listContainer.innerHTML = '';

    if (subs.length === 0) {
        listContainer.innerHTML = '<div style="color: #708499; text-align: center; margin-top: 20px;">📓 Подписки не найдены</div>';
        return;
    }

    subs.forEach(sub => {
        const card = document.createElement('div');
        card.className = 'sub-card';
        card.innerHTML = `
            <div class="sub-header">
                <span>${sub.serviceName.toUpperCase()}</span>
                <span class="price">${sub.price} ${sub.currency}</span>
            </div>
            <div class="sub-details">
                <span>Категория: ${sub.category ? sub.category : "—"}</span><br>
                <span style="font-size: 12px; display: block; margin-top: 4px;">Заметка: ${sub.notes ? sub.notes : "—"}</span>
            </div>
            <div class="card-footer">
                <span>Оплата: ${sub.nextPaymentDate}</span>
                <span class="days-left">Осталось: ${sub.daysLeft} дн.</span>
            </div>
        `;
        card.addEventListener('click', () => openEditModal(sub));
        listContainer.appendChild(card);
    });
}

// 2. Отрисовка горизонтальных тегов категорий (Spotify Style)
export function renderCategoryFilters(subs, activeCategory, onSelect) {
    const container = document.getElementById('category-filters');
    if (!container) return;
    container.innerHTML = '';

    const categories = ['Все'];
    subs.forEach(sub => {
        if (sub.category && !categories.includes(sub.category)) {
            categories.push(sub.category);
        }
    });

    categories.forEach(cat => {
        const btn = document.createElement('button');
        btn.className = `filter-chip ${cat === activeCategory ? 'active' : ''}`;
        btn.innerText = cat === 'Все' ? '🌐 Все' : `🏷️ ${cat}`;
        btn.addEventListener('click', () => onSelect(cat));
        container.appendChild(btn);
    });
}

// 3. Отрисовка списка пользователей в админке
export function renderAdminUsers(users, containerId) {
    const listContainer = document.getElementById(containerId);
    if (!listContainer) return;
    listContainer.innerHTML = '';

    if (users.length === 0) {
        listContainer.innerHTML = '<div style="color: #708499; text-align: center; margin-top: 20px;">Пользователи не найдены</div>';
        return;
    }

    users.forEach(user => {
        const card = document.createElement('div');
        card.className = 'sub-card';
        const name = user.firstName ? user.firstName : "Без имени";
        const username = user.username ? `@${user.username}` : "нет юзернейма";
        const badge = user.role === "ADMIN" ? "👑 ADMIN" : "👤 USER";
        const badgeColor = user.role === "ADMIN" ? "var(--accent-color)" : "var(--hint-color)";

        card.innerHTML = `
            <div class="sub-header">
                <span>👤 ${name}</span>
                <span style="font-size: 12px; padding: 4px 8px; border-radius: 20px; background-color: ${badgeColor}; color: #fff;">${badge}</span>
            </div>
            <div class="sub-details">
                <span>Юзернейм: <b>${username}</b></span><br>
                <span>Telegram ID: <code>${user.telegramId}</code></span>
            </div>
        `;

        card.addEventListener('click', () => openUserModal(user));
        listContainer.appendChild(card);
    });
}