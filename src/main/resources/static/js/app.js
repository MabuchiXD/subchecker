import * as api from './api.js?v=1.6';
import { renderSubscriptions, renderAdminUsers, renderCategoryFilters } from './ui.js?v=1.6';
import { setupSubModalListeners, openCreateModal } from './subModal.js?v=1.6';
import { setupUserModalListeners } from './userModal.js?v=1.6';
import { logoutAllDevices } from './api.js?v=1.6';

const tg = window.Telegram && window.Telegram.WebApp ? window.Telegram.WebApp : null;
if (tg) { tg.ready(); tg.expand(); }

let allSubscriptions = [];
let activeCategory = 'Все';
let defaultUserCurrency = 'RUB';
let currentUserId = null; // Будет заполняться из профиля /me

document.addEventListener('DOMContentLoaded', () => {
    const sessionToken = localStorage.getItem('sessionToken');

    if (sessionToken) {
        document.getElementById('login-overlay').style.display = 'none';
        initializeApplication();
    } else {
        document.getElementById('login-overlay').style.display = 'flex';
        setupLoginListener();
    }
});

function setupLoginListener() {
    document.getElementById('btn-submit-login').addEventListener('click', () => {
        const input = document.getElementById('login-code-input');
        const code = input.value.trim();

        if (code === '' || code.length !== 6) {
            alert("⚠️ Введите корректный 6-значный код безопасности!");
            return;
        }

        api.login(code)
            .then(data => {
                localStorage.setItem('sessionToken', data.sessionToken);
                document.getElementById('login-overlay').style.display = 'none';
                initializeApplication();
            })
            .catch(err => {
                alert("❌ Ошибка входа: " + err.message);
            });
    });
}

// ЗАПУСК ПРИЛОЖЕНИЯ
function initializeApplication() {
    fillTimeSelect();

    // 1. Загружаем данные вошедшего профиля из базы данных через /me
    api.fetchMe()
        .then(me => {
            currentUserId = me.telegramId;

            // 🟢 ПОКАЗЫВАЕМ РОДИТЕЛЬСКИЙ КОНТЕЙНЕР ВКЛАДОК!
            document.getElementById('admin-toggle-container').style.display = 'flex';

            // Обновляем шапку сайта реальными данными из БД!
            document.getElementById('welcome-text').innerText = `Привет, ${me.firstName || me.username}!`;
            document.getElementById('user-id-info').innerText = `ID: ${me.telegramId}`;

            // 2. Включаем кнопку админа (если у пользователя роль ADMIN)
            if (me.role === "ADMIN") {
                document.getElementById('btn-view-admin').style.display = 'block';
                setupAdminSubTabs();
            }
            setupViewNavigation();
        })
        .catch(err => {
            console.error("Error loading profile:", err);
            // Если токен сессии недействителен — сбрасываем вход
            localStorage.removeItem('sessionToken');
            window.location.reload();
        });

    // 3. Грузим личные подписки
    loadUserSubs();

    // 4. Настраиваем слушатели модалок
    setupSubModalListeners(() => {
        loadUserSubs();
        if (document.getElementById('btn-view-admin').classList.contains('active')) {
            loadAdminData();
        }
    });

    setupUserModalListeners(() => {
        if (document.getElementById('btn-view-admin').classList.contains('active')) {
            loadAdminData();
        }
    });

    document.getElementById('btn-save-settings').addEventListener('click', saveProfileSettings);
    document.getElementById('btn-join-code').addEventListener('click', handleJoinByCode);

    document.getElementById('btn-add-sub').addEventListener('click', () => {
        openCreateModal(defaultUserCurrency);
    });

    // Кнопка выхода из аккаунта (С поддержкой Telegram API)
    const btnLogout = document.getElementById('btn-logout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            const message = "Вы действительно хотите выйти из аккаунта?";

            // Если запущено внутри Telegram — используем нативный красивый диалог Telegram
            if (tg && typeof tg.showConfirm === 'function') {
                tg.showConfirm(message, (ok) => {
                    if (ok) {
                        localStorage.removeItem('sessionToken');
                        window.location.reload();
                    }
                });
            } else {
                // Если открыли просто как сайт в браузере — используем обычный confirm
                if (confirm(message)) {
                    localStorage.removeItem('sessionToken');
                    window.location.reload();
                }
            }
        });
    }

    // Кнопка выхода со всех устройств (С поддержкой Telegram API)
    const btnLogoutAll = document.getElementById('btn-logout-all');
    if (btnLogoutAll) {
        btnLogoutAll.addEventListener('click', () => {
            const message = "Вы действительно хотите завершить сессии на всех устройствах?";

            if (tg && typeof tg.showConfirm === 'function') {
                tg.showConfirm(message, (ok) => {
                    if (ok) {
                        logoutAllDevices()
                            .then(() => {
                                localStorage.removeItem('sessionToken');
                                window.location.reload();
                            })
                            .catch(err => {
                                alert("❌ Ошибка: " + err.message);
                            });
                    }
                });
            } else {
                if (confirm(message)) {
                    logoutAllDevices()
                        .then(() => {
                            localStorage.removeItem('sessionToken');
                            window.location.reload();
                        })
                        .catch(err => {
                            alert("❌ Ошибка: " + err.message);
                        });
                }
            }
        });
    }
}

function fillTimeSelect() {
    const select = document.getElementById('setting-pref-time');
    if (!select) return;
    select.innerHTML = '';
    for (let h = 0; h < 24; h++) {
        const timeStr = String(h).padStart(2, '0') + ':00';
        const option = document.createElement('option');
        option.value = timeStr;
        option.innerText = timeStr;
        select.appendChild(option);
    }
}

function loadUserSubs() {
    api.fetchUserSubs()
        .then(subs => {
            allSubscriptions = subs;
            updateCategoryFilters();
            applyFilters();
        })
        .catch(err => console.error("Error loading user subs:", err));
}

function updateCategoryFilters() {
    renderCategoryFilters(allSubscriptions, activeCategory, (category) => {
        activeCategory = category;
        updateCategoryFilters();
        applyFilters();
    });
}

function applyFilters() {
    const query = document.getElementById('search-input').value.toLowerCase().trim();
    const filtered = allSubscriptions.filter(sub => {
        const matchesSearch = sub.serviceName.toLowerCase().includes(query) ||
                              (sub.notes && sub.notes.toLowerCase().includes(query));
        const matchesCategory = (activeCategory === 'Все') || (sub.category === activeCategory);
        return matchesSearch && matchesCategory;
    });
    renderSubscriptions(filtered, 'subs-list');
}

function handleJoinByCode() {
    const code = prompt("Введите секретный инвайт-код:");
    if (code && code.trim() !== '') {
        api.joinByInviteCode(code.trim())
            .then(data => {
                alert(`✅ Успешно! Вы добавлены в подписку ${data.serviceName}!`);
                loadUserSubs();
            })
            .catch(err => {
                alert(`❌ Ошибка: ${err.message}`);
            });
    }
}

function setupViewNavigation() {
    const btnUser = document.getElementById('btn-view-user');
    const btnSettings = document.getElementById('btn-view-settings');
    const btnAdmin = document.getElementById('btn-view-admin');

    const viewUser = document.getElementById('user-view');
    const viewSettings = document.getElementById('settings-view');
    const viewAdmin = document.getElementById('admin-view');

    btnUser.addEventListener('click', () => {
        switchView(btnUser, viewUser);
        loadUserSubs();
    });

    btnSettings.addEventListener('click', () => {
        switchView(btnSettings, viewSettings);
        loadProfileSettings();
    });

    btnAdmin.addEventListener('click', () => {
        switchView(btnAdmin, viewAdmin);
        loadAdminData();
    });

    function switchView(activeBtn, activeView) {
        [btnUser, btnSettings, btnAdmin].forEach(b => { if (b) b.classList.remove('active'); });
        [viewUser, viewSettings, viewAdmin].forEach(v => { if (v) v.style.display = 'none'; });
        if (activeBtn) activeBtn.classList.add('active');
        if (activeView) activeView.style.display = 'block';
    }
}

function loadProfileSettings() {
    api.fetchUserSettings()
        .then(settings => {
            document.getElementById('setting-pref-time').value = settings.preferredTime;
            document.getElementById('setting-timezone').value = settings.timezoneOffset;
            document.getElementById('setting-default-currency').value = settings.defaultCurrency;
            defaultUserCurrency = settings.defaultCurrency;
        })
        .catch(err => console.error("Error loading profile settings:", err));
}

function saveProfileSettings() {
    const data = {
        preferredTime: document.getElementById('setting-pref-time').value,
        timezoneOffset: parseInt(document.getElementById('setting-timezone').value),
        defaultCurrency: document.getElementById('setting-default-currency').value
    };

    api.saveUserSettings(data)
        .then(res => {
            if (!res.ok) throw new Error();
            alert("✅ Настройки профиля сохранены!");
            defaultUserCurrency = data.defaultCurrency;
        })
        .catch(() => alert("❌ Ошибка сохранения настроек."));
}

function loadAdminData() {
    api.fetchAdminStats()
        .then(stats => {
            document.getElementById('stat-users').innerText = stats.usersCount;
            document.getElementById('stat-subs').innerText = stats.subsCount;
        });

    api.fetchAdminAllUsers()
        .then(users => renderAdminUsers(users, 'admin-users-list'));
}

function setupAdminSubTabs() {
    const tabSubs = document.getElementById('tab-admin-subs');
    const tabUsers = document.getElementById('tab-admin-users');
    const subsContainer = document.getElementById('admin-subs-container');
    const usersContainer = document.getElementById('admin-users-container');

    if (tabSubs) tabSubs.addEventListener('click', () => {
        if (tabUsers) tabUsers.classList.remove('active');
        tabSubs.classList.add('active');
        if (usersContainer) usersContainer.style.display = 'none';
        if (subsContainer) subsContainer.style.display = 'block';
        loadAdminData();
    });

    if (tabUsers) tabUsers.addEventListener('click', () => {
        if (tabSubs) tabSubs.classList.remove('active');
        tabUsers.classList.add('active');
        if (subsContainer) subsContainer.style.display = 'none';
        if (usersContainer) usersContainer.style.display = 'block';
        loadAdminData();
    });
}

document.getElementById('search-input').addEventListener('input', applyFilters);