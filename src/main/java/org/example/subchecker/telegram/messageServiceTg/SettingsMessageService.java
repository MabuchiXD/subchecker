package org.example.subchecker.telegram.messageServiceTg;

import org.example.subchecker.core.entity.BotSession;
import org.example.subchecker.core.entity.User;
import org.example.subchecker.telegram.botKeyboards.SettingsKeyboards;
import org.example.subchecker.telegram.feature.stateDiary.CallbackData;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalTime;

@Service
public class SettingsMessageService {

    public SendMessage getSettingsMenu(Long chatId, User user, BotSession session) {
        // 1. Проверяем ВРЕМЯ
        LocalTime currentTime = user.getPreferredNotificationTime();
        String timeStr = (session.getTempPrefTime() != null)
                ? session.getTempPrefTime().toString() + " 🆕"
                : (currentTime != null ? currentTime.toString() : "10:00");
        // 2. Проверяем ЧАСОВОЙ ПОЯС
        Integer currentTz = user.getTimezoneOffset();
        Integer draftTz = session.getTempTimezoneOffset();
        String tzStr = (draftTz != null)
                ? "UTC " + (draftTz >= 0 ? "+" : "") + draftTz + " 🆕"
                : "UTC " + (currentTz >= 0 ? "+" : "") + currentTz;

        // 3. Проверяем ВАЛЮТУ
        String currStr = (session.getTempDefaultCurrency() != null)
                ? session.getTempDefaultCurrency().name() + " 🆕"
                : user.getDefaultCurrency().name();

        // Есть ли вообще изменения?
        boolean hasChanges = session.getTempPrefTime() != null
                || session.getTempTimezoneOffset() != null
                || session.getTempDefaultCurrency() != null;

        String text = "⚙️ *Твои настройки профиля:*\n\n" +
                "🕒 Время уведомлений: *" + timeStr + "*\n" +
                "🌍 Часовой пояс: *" + tzStr + "*\n" +
                "💱 Валюта по умолчанию: *" + currStr + "*\n\n" +
                (hasChanges
                        ? "⚠️ *Есть несохраненные изменения!*\nНажми кнопку «СОХРАНИТЬ», чтобы применить их."
                        : "_Выбери пункт для изменения:_");

        return create(chatId, text, SettingsKeyboards.mainSettingsMenu(hasChanges));
    }

    public SendMessage getTimeSelection(Long chatId) {
        return SendMessage.builder().chatId(chatId.toString()).text("🕒 *Выбери час для уведомлений:*")
                .replyMarkup(SettingsKeyboards.timeSelectionGrid()).parseMode("Markdown").build();
    }

    public SendMessage getTimezoneSelection(Long chatId) {
        return SendMessage.builder().chatId(chatId.toString()).text("🌍 *Выбери свой часовой пояс:*")
                .replyMarkup(SettingsKeyboards.timezoneSelection()).parseMode("Markdown").build();
    }

    public SendMessage getCurrencySelection(Long chatId) {
        return SendMessage.builder().chatId(chatId.toString()).text("💱 *Выбери валюту по умолчанию:*")
                .replyMarkup(SettingsKeyboards.currencySelection(CallbackData.SET_CURR_DEFAULT_PREFIX)).parseMode("Markdown").build();
    }
    public SendMessage getCustomTimezonePrompt(Long chatId) {
        String text = "🌍 *Введи свой часовой пояс относительно UTC.*\n\n" +
                "Например, если в Москве сейчас 12:00, а в Гринвиче 09:00 — пиши `+3`.\n" +
                "Если время отстает — пиши `-5`.";
        // Кнопку "Назад" берем из ContextualKeyboards, так как она стандартная для ввода текста
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(org.example.subchecker.telegram.botKeyboards.ContextualKeyboards.justBackKeyboard())
                .parseMode("Markdown")
                .build();
    }
    public SendMessage getSettingsSavedSuccess(Long chatId) {
        // Возвращаем текст и ГЛАВНОЕ МЕНЮ (PrimaryKeyboard), чтобы юзер мог сразу продолжить работу
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("✅ *Настройки профиля успешно сохранены!*")
                .replyMarkup(org.example.subchecker.telegram.botKeyboards.PrimaryKeyboards.mainMenu())
                .parseMode("Markdown")
                .build();
    }
    private SendMessage create(Long chatId, String text, org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard kb) {
        return SendMessage.builder().chatId(chatId.toString()).text(text).replyMarkup(kb).parseMode("Markdown").build();
    }
}