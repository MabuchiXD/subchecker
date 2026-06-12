package org.example.subchecker.telegram.botKeyboards;

import org.example.subchecker.core.dto.SubscriptionDTO;
import org.example.subchecker.telegram.feature.stateDiary.CallbackData;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class ContextualKeyboards {

    public static InlineKeyboardMarkup subscriptionControl(SubscriptionDTO sub, boolean isOwner) {
        List<List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton>> rows = new ArrayList<>();
        Long id = sub.getId();

        String actionText = isOwner ? "🗑 Удалить" : "🚪 Выйти";
        String actionData = isOwner ? CallbackData.DELETE_PREFIX : CallbackData.LEAVE_PREFIX;

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("✅ Оплатил", CallbackData.PAY_PREFIX + id),
                KeyboardUtils.createInlineBtn(actionText, actionData + id)
        ));

        String bStat = (sub.getIsHardcore() != null && sub.getIsHardcore()) ? "🚀 Бомбер: ВКЛ" : "💤 Бомбер: ВЫКЛ";
        rows.add(List.of(KeyboardUtils.createInlineBtn(bStat, CallbackData.TOGGLE_BOMBER_PREFIX + id)));

        List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(KeyboardUtils.createInlineBtn("⚙️ Изменить", CallbackData.EDIT_SUB_MENU_PREFIX + id));
        if (isOwner) {
            row3.add(KeyboardUtils.createInlineBtn("👥 Группа", CallbackData.FAMILY_MGMT_PREFIX + id));
        }
        rows.add(row3);

        rows.add(List.of(KeyboardUtils.createInlineBtn("🔙 Назад к списку", CallbackData.BACK_TO_LIST)));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }


    public static InlineKeyboardMarkup categorySelection() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(
                KeyboardUtils.createInlineBtn("🎬 Кино", CallbackData.CATEGORY_PREFIX + "Кино"),
                KeyboardUtils.createInlineBtn("🎮 Игры", CallbackData.CATEGORY_PREFIX + "Игры")
        ));
        rows.add(List.of(
                KeyboardUtils.createInlineBtn("🛡 VPN", CallbackData.CATEGORY_PREFIX + "VPN"),
                KeyboardUtils.createInlineBtn("🎵 Музыка", CallbackData.CATEGORY_PREFIX + "Музыка")
        ));

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("✏️ Своя категория", CallbackData.CATEGORY_CUSTOM)
        ));

        rows.add(getBackRow());
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }


    public static InlineKeyboardMarkup periodSelection() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(
                KeyboardUtils.createInlineBtn("⌛ 14 дн", CallbackData.PERIOD_PREFIX + "14"),
                KeyboardUtils.createInlineBtn("📅 1 мес", CallbackData.PERIOD_PREFIX + "30"),
                KeyboardUtils.createInlineBtn("📅 3 мес", CallbackData.PERIOD_PREFIX + "90")
        ));
        rows.add(List.of(
                KeyboardUtils.createInlineBtn("🗓 1 год", CallbackData.PERIOD_PREFIX + "365"),
                KeyboardUtils.createInlineBtn("✏️ Свой срок", CallbackData.PERIOD_CUSTOM)
        ));
        rows.add(getBackRow());
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup editSubscriptionMenu(Long subId, boolean isHardcore) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Название и Цена
        rows.add(List.of(
                KeyboardUtils.createInlineBtn("📝 Название", CallbackData.EDIT_FIELD_NAME + subId),
                KeyboardUtils.createInlineBtn("💰 Цена", CallbackData.EDIT_FIELD_PRICE + subId)
        ));

        // Переключатель Бомбера
        String bomberStatus = isHardcore ? "🚀 Бомбер: ВКЛ" : "💤 Бомбер: ВЫКЛ";
        rows.add(List.of(
                KeyboardUtils.createInlineBtn(bomberStatus, CallbackData.TOGGLE_BOMBER_PREFIX + subId)
        ));

        // Ссылка и Заметки
        rows.add(List.of(
                KeyboardUtils.createInlineBtn("🔗 Ссылка", CallbackData.EDIT_FIELD_URL + subId),
                KeyboardUtils.createInlineBtn("🗒 Заметки", CallbackData.EDIT_FIELD_NOTES + subId)
        ));

        // Интервал и Назад
        rows.add(List.of(
                KeyboardUtils.createInlineBtn("⏱ Интервал", CallbackData.EDIT_FIELD_BOMBER + subId),
                KeyboardUtils.createInlineBtn("⬅️ Назад", CallbackData.SELECT_PREFIX + subId)
        ));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }


    public static InlineKeyboardMarkup bomberIntervalSelection(Long subId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(KeyboardUtils.createInlineBtn("🚀 15 мин", CallbackData.SET_BOMBER_INT_PREFIX + subId + "_15"),
                KeyboardUtils.createInlineBtn("🧨 30 мин", CallbackData.SET_BOMBER_INT_PREFIX + subId + "_30")));
        rows.add(List.of(KeyboardUtils.createInlineBtn("📢 1 час", CallbackData.SET_BOMBER_INT_PREFIX + subId + "_60"),
                KeyboardUtils.createInlineBtn("🔔 2 часа", CallbackData.SET_BOMBER_INT_PREFIX + subId + "_120")));
        rows.add(List.of(KeyboardUtils.createInlineBtn("⬅️ Назад", CallbackData.EDIT_SUB_MENU_PREFIX + subId)));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup subscriptionList(List<SubscriptionDTO> subs) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (SubscriptionDTO sub : subs) {
            currentRow.add(KeyboardUtils.createInlineBtn(sub.getServiceName(), CallbackData.SELECT_PREFIX + sub.getId()));
            if (currentRow.size() == 2) { rows.add(new ArrayList<>(currentRow)); currentRow.clear(); }
        }
        if (!currentRow.isEmpty()) rows.add(currentRow);
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }


    public static ReplyKeyboardMarkup skipUrlKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("Нет ссылки 🚫");
        row.add("⬅️ Назад");
        return ReplyKeyboardMarkup.builder().keyboard(List.of(row)).resizeKeyboard(true).build();
    }

    public static ReplyKeyboardMarkup skipNotesKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("Без заметок 📝");
        row.add("⬅️ Назад");
        return ReplyKeyboardMarkup.builder().keyboard(List.of(row)).resizeKeyboard(true).build();
    }

    public static ReplyKeyboardMarkup justBackKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("⬅️ Назад");
        return ReplyKeyboardMarkup.builder().keyboard(List.of(row)).resizeKeyboard(true).build();
    }

    private static List<InlineKeyboardButton> getBackRow() {
        return List.of(KeyboardUtils.createInlineBtn("⬅️ Назад", CallbackData.STEP_BACK));
    }

    public static InlineKeyboardMarkup changeCurrencyInline() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("💱 Изменить валюту для этой подписки", CallbackData.EDIT_FIELD_CURR_CREATION)
        ));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
    public static InlineKeyboardMarkup priceCurrencyButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("🇷🇺 RUB", "SUB_CURR_RUB"),
                KeyboardUtils.createInlineBtn("🇺🇸 USD", "SUB_CURR_USD"),
                KeyboardUtils.createInlineBtn("🇪🇺 EUR", "SUB_CURR_EUR"),
                KeyboardUtils.createInlineBtn("🇰🇿 KZT", "SUB_CURR_KZT")
        ));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}