import * as api from './api.js?v=1.8';

const tg = window.Telegram && window.Telegram.WebApp ? window.Telegram.WebApp : null;
let selectedSubscription = null;
let modalMode = 'EDIT';

function getFutureDateString(days) {
    const d = new Date();
    d.setDate(d.getDate() + days);
    return d.toISOString().split('T')[0];
}

export function openCreateModal(defaultCurrency) {
    modalMode = 'CREATE';
    selectedSubscription = null;

    document.getElementById('modal-title').innerText = "➕ Добавить подписку";

    document.getElementById('edit-name').value = '';
    document.getElementById('edit-price').value = '';
    document.getElementById('edit-notes').value = '';
    document.getElementById('edit-category').value = '';
    document.getElementById('edit-url').value = '';
    document.getElementById('edit-currency').value = defaultCurrency || 'RUB';
    document.getElementById('edit-period').value = '30';
    document.getElementById('edit-next-payment').value = getFutureDateString(30);

    document.getElementById('edit-currency-container').style.display = 'block';
    document.getElementById('edit-period-container').style.display = 'block';
    document.getElementById('edit-category-container').style.display = 'block';
    document.getElementById('bomber-admin-section').style.display = 'none';

    document.getElementById('btn-pay').style.display = 'none';
    document.getElementById('btn-invite').style.display = 'none';
    document.getElementById('btn-delete').style.display = 'none';

    document.getElementById('btn-save').innerText = "➕ Создать подписку";
    document.getElementById('modal-overlay').classList.add('active');
}

export function openEditModal(sub) {
    modalMode = 'EDIT';
    selectedSubscription = sub;

    document.getElementById('modal-title').innerText = `⚙️ ${sub.serviceName.toUpperCase()}`;
    document.getElementById('edit-name').value = sub.serviceName;
    document.getElementById('edit-price').value = sub.price;
    document.getElementById('edit-notes').value = sub.notes || '';
    document.getElementById('edit-category').value = sub.category || '';
    document.getElementById('edit-url').value = sub.paymentUrl || '';
    document.getElementById('edit-currency').value = sub.currency || 'RUB';

    if (sub.nextPaymentDate) {
        document.getElementById('edit-next-payment').value = sub.nextPaymentDate;
    } else {
        document.getElementById('edit-next-payment').value = getFutureDateString(30);
    }

    document.getElementById('edit-currency-container').style.display = 'block';
    document.getElementById('edit-period-container').style.display = 'none';

    document.getElementById('bomber-admin-section').style.display = 'block';
    document.getElementById('btn-pay').style.display = 'block';
    document.getElementById('btn-invite').style.display = 'block';
    document.getElementById('btn-delete').style.display = 'block';

    document.getElementById('edit-bomber-active').checked = sub.isHardcore || false;
    document.getElementById('edit-bomber-interval').value = sub.bomberIntervalMinutes || "60";

    document.getElementById('btn-save').innerText = "💾 Сохранить изменения";
    document.getElementById('modal-overlay').classList.add('active');
}

export function closeModal() {
    document.getElementById('modal-overlay').classList.remove('active');
    selectedSubscription = null;
}

export function setupSubModalListeners(onRefresh) {
    document.getElementById('btn-close-modal').addEventListener('click', closeModal);
    document.getElementById('modal-overlay').addEventListener('click', (e) => {
        if (e.target === document.getElementById('modal-overlay')) closeModal();
    });

    document.getElementById('edit-period').addEventListener('change', (e) => {
        if (modalMode === 'CREATE') {
            const days = parseInt(e.target.value);
            document.getElementById('edit-next-payment').value = getFutureDateString(days);
        }
    });

    document.getElementById('btn-save').addEventListener('click', () => {
        const name = document.getElementById('edit-name').value.trim();
        const priceVal = document.getElementById('edit-price').value;
        const nextPaymentDate = document.getElementById('edit-next-payment').value;

        if (name === '' || priceVal === '') {
            alert("⚠️ Название и стоимость обязательны!");
            return;
        }

        const price = parseFloat(priceVal);

        const btnSave = document.getElementById('btn-save');
        btnSave.disabled = true;
        const originalText = btnSave.innerText;
        btnSave.innerText = modalMode === 'CREATE' ? "➕ Создание..." : "💾 Сохранение...";

        if (modalMode === 'CREATE') {
            const newSubData = {
                serviceName: name,
                price: price,
                currency: document.getElementById('edit-currency').value,
                category: document.getElementById('edit-category').value.trim() || 'Разное',
                periodDays: parseInt(document.getElementById('edit-period').value),
                nextPaymentDate: nextPaymentDate || null,
                paymentUrl: document.getElementById('edit-url').value.trim() || null,
                notes: document.getElementById('edit-notes').value.trim() || null
            };

            api.createSubscription(newSubData)
                .then(res => {
                    if (!res.ok) throw new Error();
                    alert("✅ Подписка добавлена!");
                    closeModal();
                    onRefresh();
                })
                .catch(() => alert("❌ Ошибка добавления подписки."))
                .finally(() => {
                    btnSave.disabled = false;
                    btnSave.innerText = originalText;
                });

        } else {
            const updatedData = {
                serviceName: name,
                price: price,
                currency: document.getElementById('edit-currency').value,
                nextPaymentDate: nextPaymentDate || null,
                notes: document.getElementById('edit-notes').value.trim() || null,
                paymentUrl: document.getElementById('edit-url').value.trim() || null,
                isHardcore: document.getElementById('edit-bomber-active').checked,
                bomberIntervalMinutes: parseInt(document.getElementById('edit-bomber-interval').value)
            };

            api.saveSubscriptionEdit(selectedSubscription.id, updatedData)
                .then(res => {
                    if (!res.ok) throw new Error();
                    alert("✅ Изменения сохранены!");
                    closeModal();
                    onRefresh();
                })
                .catch(() => alert("❌ Ошибка сохранения."))
                .finally(() => {
                    btnSave.disabled = false;
                    btnSave.innerText = originalText;
                });
        }
    });

    // ОПЛАТИЛ (С БЕЗОПАСНОЙ ПРОВЕРКОЙ ВЕРСИИ TELEGRAM API 🔒)
    document.getElementById('btn-pay').addEventListener('click', () => {
        if (!selectedSubscription) return;

        const defaultPeriod = selectedSubscription.periodDays || 30;
        const message = `На какой срок ты продлил подписку ${selectedSubscription.serviceName.toUpperCase()}?`;

        // Проверяем, поддерживает ли клиент версию 6.2+
        if (tg && tg.isVersionAtLeast('6.2') && typeof tg.showPopup === 'function') {
            tg.showPopup({
                title: "🔄 Продление подписки",
                message: message,
                buttons: [
                    { id: "30", type: "default", text: "📅 На 30 дней (Месяц)" },
                    { id: "90", type: "default", text: "🗓 На 90 дней (3 месяца)" },
                    { id: "cancel", type: "destructive", text: "❌ Отмена" }
                ]
            }, (buttonId) => {
                if (buttonId === 'cancel') return;
                const days = parseInt(buttonId);

                api.renewSubscription(selectedSubscription.id, days)
                    .then(res => {
                        if (!res.ok) throw new Error();
                        alert(`🔄 Подписка успешно продлена на ${days} дней!`);
                        closeModal();
                        onRefresh();
                    })
                    .catch(() => alert("❌ Ошибка продления подписки."));
            });
        } else {
            // Если Telegram API старый (6.0) — откатываемся на стандартный prompt
            const choice = prompt(message, defaultPeriod);
            if (choice === null) return;

            const days = parseInt(choice.trim());
            if (isNaN(days) || days <= 0) {
                alert("⚠️ Введите корректное положительное число дней!");
                return;
            }

            api.renewSubscription(selectedSubscription.id, days)
                .then(res => {
                    if (!res.ok) throw new Error();
                    alert(`🔄 Подписка успешно продлена на ${days} дней!`);
                    closeModal();
                    onRefresh();
                })
                .catch(() => alert("❌ Ошибка продления подписки."));
        }
    });

    document.getElementById('btn-invite').addEventListener('click', () => {
        if (!selectedSubscription) return;
        api.generateInviteCode(selectedSubscription.id)
            .then(data => {
                alert(`👥 Код приглашения (перешлите другу):\n\n${data.code}`);
            })
            .catch(() => alert("❌ Ошибка генерации кода."));
    });

    // УДАЛИТЬ ПОДПИСКУ (С БЕЗОПАСНОЙ ПРОВЕРКОЙ ВЕРСИИ TELEGRAM API 🔒)
    document.getElementById('btn-delete').addEventListener('click', () => {
        if (!selectedSubscription) return;
        const message = "Вы точно хотите безвозвратно удалить эту подписку?";

        // Проверяем, поддерживает ли клиент версию 6.2+
        if (tg && tg.isVersionAtLeast('6.2') && typeof tg.showConfirm === 'function') {
            tg.showConfirm(message, (ok) => {
                if (ok) {
                    api.deleteSubscription(selectedSubscription.id)
                        .then(res => {
                            if (!res.ok) throw new Error();
                            alert("🗑 Подписка успешно удалена!");
                            closeModal();
                            onRefresh();
                        })
                        .catch(() => alert("❌ Ошибка удаления подписки."));
                }
            });
        } else {
            // Если Telegram API старый (6.0) — откатываемся на стандартный confirm
            if (confirm(message)) {
                api.deleteSubscription(selectedSubscription.id)
                    .then(res => {
                        if (!res.ok) throw new Error();
                        alert("🗑 Подписка успешно удалена!");
                        closeModal();
                        onRefresh();
                    })
                    .catch(() => alert("❌ Ошибка удаления подписки."));
            }
        }
    });
}