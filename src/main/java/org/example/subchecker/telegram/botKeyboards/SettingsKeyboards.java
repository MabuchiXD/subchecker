package org.example.subchecker.telegram.botKeyboards;

import org.example.subchecker.core.model.Currency;
import org.example.subchecker.telegram.feature.stateDiary.CallbackData;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class SettingsKeyboards {

    public static InlineKeyboardMarkup mainSettingsMenu(boolean hasChanges) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("🕒 Время уведомлений", CallbackData.EDIT_PREF_TIME),
                KeyboardUtils.createInlineBtn("🌍 Часовой пояс", CallbackData.EDIT_TIMEZONE)
        ));

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("💱 Валюта по умолчанию", CallbackData.EDIT_DEFAULT_CURR)
        ));

        if (hasChanges) {
            rows.add(List.of(
                    KeyboardUtils.createInlineBtn("✅ СОХРАНИТЬ ИЗМЕНЕНИЯ", CallbackData.SAVE_SETTINGS)
            ));
        }

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("⬅️ Назад", CallbackData.STEP_BACK)
        ));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup timeSelectionGrid() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (int h = 0; h < 24; h++) {
            String timeStr = String.format("%02d:00", h);
            currentRow.add(KeyboardUtils.createInlineBtn(timeStr, CallbackData.SET_TIME_PREFIX + h));

            if (currentRow.size() == 4) {
                rows.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }
        rows.add(List.of(KeyboardUtils.createInlineBtn("⬅️ Назад", CallbackData.STEP_BACK)));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup timezoneSelection() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                KeyboardUtils.createInlineBtn("МСК (UTC+3)", CallbackData.SET_TZ_PREFIX + "3"),
                KeyboardUtils.createInlineBtn("Европа (UTC+1)", CallbackData.SET_TZ_PREFIX + "1")
        ));
        rows.add(List.of(
                KeyboardUtils.createInlineBtn("Казахстан (UTC+5)", CallbackData.SET_TZ_PREFIX + "5"),
                KeyboardUtils.createInlineBtn("⌨️ Другой (ввод)", CallbackData.EDIT_CUSTOM_TZ)
        ));
        rows.add(List.of(KeyboardUtils.createInlineBtn("⬅️ Назад", CallbackData.STEP_BACK)));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup currencySelection(String prefix) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (Currency curr : Currency.values()) {
            currentRow.add(KeyboardUtils.createInlineBtn(curr.name(), prefix + curr.name()));

            if (currentRow.size() == 2) {
                rows.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }
        if (!currentRow.isEmpty()) rows.add(currentRow);

        rows.add(List.of(KeyboardUtils.createInlineBtn("⬅️ Назад", CallbackData.STEP_BACK)));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}