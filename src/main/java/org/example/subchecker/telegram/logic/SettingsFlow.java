package org.example.subchecker.telegram.logic;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.entity.BotSession;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.UserService;
import org.example.subchecker.telegram.messageServiceTg.CommonMessageService;
import org.example.subchecker.telegram.messageServiceTg.SettingsMessageService; // НОВЫЙ ИМПОРТ
import org.example.subchecker.telegram.stateDiary.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class SettingsFlow {

    private final UserService userService;
    private final BotSessionService sessionService;
    private final CommonMessageService commonMessageService;
    private final SettingsMessageService settingsMessageService; // Внедряем голос настроек

    public SendMessage handle(Long chatId, String text, String state) {
        BotSession session = sessionService.getSession(chatId);

        if (BotState.WAIT_PREF_TIME.equals(state)) {
            try {
                int hour = Integer.parseInt(text.replaceAll("[^0-9]", ""));
                if (hour < 0 || hour > 23) throw new Exception();

                sessionService.saveTempPrefTime(chatId, LocalTime.of(hour, 0));
                sessionService.updateState(chatId, BotState.IDLE);

                return settingsMessageService.getSettingsMenu(chatId, userService.getByTgId(chatId), sessionService.getSession(chatId));
            } catch (Exception e) {
                return commonMessageService.getErrorMessage(chatId, "Введи просто час от 0 до 23.");
            }
        }

        if (BotState.WAIT_CUSTOM_TZ.equals(state)) {
            try {
                int offset = Integer.parseInt(text.replace("+", "").trim());
                if (offset < -12 || offset > 14) throw new Exception(); // Валидация часовых поясов

                sessionService.saveTempTimezone(chatId, offset);
                sessionService.updateState(chatId, BotState.IDLE);

                return settingsMessageService.getSettingsMenu(chatId, userService.getByTgId(chatId), sessionService.getSession(chatId));
            } catch (Exception e) {
                return commonMessageService.getErrorMessage(chatId, "Введи число (напр. +3 или -5):");
            }
        }
        return null;
    }
}