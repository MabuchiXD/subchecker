package org.example.subchecker.telegram.handlers;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.SubscriptionService;
import org.example.subchecker.service.UserService;
import org.example.subchecker.web.security.OtpService;
import org.example.subchecker.telegram.feature.subscription.SubscriptionFormatter;
import org.example.subchecker.telegram.messageServiceTg.CommonMessageService;
import org.example.subchecker.telegram.messageServiceTg.SettingsMessageService;
import org.example.subchecker.telegram.messageServiceTg.SubscriptionFlowMessageService;
import org.example.subchecker.telegram.feature.stateDiary.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MainMenuHandler {

    private final UserService userService;
    private final BotSessionService sessionService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionFormatter subscriptionFormatter;
    private final CommonMessageService commonMessageService;
    private final SubscriptionFlowMessageService flowMessageService;
    private final SettingsMessageService settingsMessageService;
    private final OtpService otpService;

    public List<SendMessage> handle(Long chatId, String text) {

        if (text.contains("/start")) {
            sessionService.updateState(chatId, BotState.IDLE);
            return List.of(commonMessageService.getStartMessage(chatId)); // Просто возвращаем готовый объект!
        }

        if (text.contains("/code")) {
            String code = otpService.generateCode(chatId);

            String messageText = "🔑 *Твой одноразовый код авторизации:*\n\n" +
                    "`" + code + "`\n\n" +
                    "_Введите этот код на сайте в течение 1 минуты для безопасного входа._";

            return List.of(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(messageText)
                    .parseMode("Markdown")
                    .build());
        }

        if (text.contains("Добавить подписку")) {
            sessionService.updateState(chatId, BotState.WAIT_NAME);
            return List.of(flowMessageService.getAskName(chatId));
        }

        if (text.contains("Мои подписки")) {
            return subscriptionFormatter.formatSummary(chatId, subscriptionService.getMySubscriptions(chatId));
        }

        if (text.contains("Настройки")) {
            return List.of(settingsMessageService.getSettingsMenu(
                    chatId,
                    userService.getByTgId(chatId),
                    sessionService.getSession(chatId)
            ));
        }

        if (text.contains("Войти в группу")) {
            sessionService.updateState(chatId, BotState.WAIT_INVITE_CODE);
            return List.of(flowMessageService.getAskInviteCode(chatId));
        }

        return null;
    }
}