package org.example.subchecker.telegram.logic.handlers.callbacks;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.model.Currency;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.UserService;
import org.example.subchecker.telegram.messageServiceTg.CommonMessageService;
import org.example.subchecker.telegram.messageServiceTg.SettingsMessageService;
import org.example.subchecker.telegram.stateDiary.BotState;
import org.example.subchecker.telegram.stateDiary.CallbackData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SettingsCallbackHandler {

    private final UserService userService;
    private final BotSessionService sessionService;
    private final SettingsMessageService settingsMessages;
    private final CommonMessageService commonMessages;

    public List<SendMessage> handle(Long chatId, String data) {

        if (data.equals(CallbackData.EDIT_PREF_TIME)) return List.of(settingsMessages.getTimeSelection(chatId));
        if (data.equals(CallbackData.EDIT_TIMEZONE)) return List.of(settingsMessages.getTimezoneSelection(chatId));
        if (data.equals(CallbackData.EDIT_DEFAULT_CURR)) return List.of(settingsMessages.getCurrencySelection(chatId));

        if (data.equals(CallbackData.EDIT_CUSTOM_TZ)) {
            sessionService.updateState(chatId, BotState.WAIT_CUSTOM_TZ);
            return List.of(settingsMessages.getCustomTimezonePrompt(chatId));
        }

        if (data.startsWith(CallbackData.SET_TIME_PREFIX)) {
            int h = extractInt(data, CallbackData.SET_TIME_PREFIX);
            sessionService.saveTempPrefTime(chatId, LocalTime.of(h, 0));
            return returnToSettings(chatId);
        }

        if (data.startsWith(CallbackData.SET_TZ_PREFIX)) {
            int offset = extractInt(data, CallbackData.SET_TZ_PREFIX);
            sessionService.saveTempTimezone(chatId, offset);
            return returnToSettings(chatId);
        }

        if (data.startsWith(CallbackData.SET_CURR_DEFAULT_PREFIX)) {
            String curr = extractString(data, CallbackData.SET_CURR_DEFAULT_PREFIX);
            sessionService.saveTempDefaultCurrency(chatId, Currency.valueOf(curr));
            return returnToSettings(chatId);
        }

        if (data.equals(CallbackData.SAVE_SETTINGS)) {
            userService.saveSettingsFromSession(chatId, sessionService.getSession(chatId));
            return List.of(settingsMessages.getSettingsSavedSuccess(chatId));
        }

        return null;
    }

    private List<SendMessage> returnToSettings(Long chatId) {
        return List.of(settingsMessages.getSettingsMenu(
                chatId,
                userService.getByTgId(chatId),
                sessionService.getSession(chatId)
        ));
    }

    private int extractInt(String data, String prefix) {
        return Integer.parseInt(data.replace(prefix, ""));
    }

    private String extractString(String data, String prefix) {
        return data.replace(prefix, "");
    }
}