package org.example.subchecker.telegram.logic;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.UserService;
import org.example.subchecker.telegram.logic.handlers.*;
import org.example.subchecker.telegram.messageServiceTg.CommonMessageService;
import org.example.subchecker.telegram.navigation.NavigationService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final UserService userService;
    private final BotSessionService sessionService;
    private final MainMenuHandler mainMenuHandler;
    private final TextStateHandler textStateHandler;
    private final CallbackDispatcher callbackDispatcher;
    private final NavigationService navigationService;
    private final CommonMessageService commonMessageService;

    @Transactional
    public List<SendMessage> processUpdate(Long chatId, String text, String username, String firstName) {
        userService.registerOrUpdateUser(chatId, username, firstName);
        var session = sessionService.getSession(chatId);

        if ("⬅️ Назад".equals(text)) {
            return List.of(navigationService.handleBack(chatId, session.getState()));
        }

        List<SendMessage> menuResponse = mainMenuHandler.handle(chatId, text);
        if (menuResponse != null) return menuResponse;

        List<SendMessage> stateResponse = textStateHandler.handle(chatId, text, session.getState());
        if (stateResponse != null) return stateResponse;

        return List.of(commonMessageService.getMainMenuMessage(chatId, "Выберите действие в меню:"));
    }

    public List<SendMessage> processCallback(Long chatId, String data) {
        return callbackDispatcher.dispatch(chatId, data);
    }
}