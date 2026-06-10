package org.example.subchecker.telegram.logic.handlers;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.telegram.logic.handlers.callbacks.EditCallbackHandler;
import org.example.subchecker.telegram.logic.handlers.callbacks.FamilyCallbackHandler;
import org.example.subchecker.telegram.logic.handlers.callbacks.SettingsCallbackHandler;
import org.example.subchecker.telegram.logic.handlers.callbacks.SubCallbackHandler;
import org.example.subchecker.telegram.navigation.NavigationService;
import org.example.subchecker.telegram.stateDiary.CallbackData;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CallbackDispatcher {
    private final SubCallbackHandler subHandler;
    private final SettingsCallbackHandler settingsHandler;
    private final EditCallbackHandler editHandler;
    private final FamilyCallbackHandler familyHandler;
    private final NavigationService navService;
    private final BotSessionService sessionService;

    public List<SendMessage> dispatch(Long chatId, String data) {
        if (data.equals(CallbackData.STEP_BACK) || data.equals(CallbackData.BACK_TO_LIST))
            return List.of(navService.handleBack(chatId, sessionService.getSession(chatId).getState()));

        List<SendMessage> result;
        if ((result = subHandler.handle(chatId, data)) != null) return result;
        if ((result = settingsHandler.handle(chatId, data)) != null) return result;
        if ((result = editHandler.handle(chatId, data)) != null) return result ;
        if ((result = familyHandler.handle(chatId, data)) != null) return result;

        return new java.util.ArrayList<>();
    }
}