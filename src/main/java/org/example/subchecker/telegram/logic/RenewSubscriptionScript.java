package org.example.subchecker.telegram.logic;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.SubscriptionService;
import org.example.subchecker.telegram.messageServiceTg.CommonMessageService;
import org.example.subchecker.telegram.messageServiceTg.SubscriptionFlowMessageService;
import org.example.subchecker.telegram.messageServiceTg.SubscriptionMessageService; // НОВЫЙ ИМПОРТ
import org.example.subchecker.telegram.stateDiary.BotState;
import org.example.subchecker.telegram.stateDiary.CallbackData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class RenewSubscriptionScript {

    private final BotSessionService botSessionService;
    private final SubscriptionService subscriptionService;
    private final CommonMessageService commonMessageService;
    private final SubscriptionFlowMessageService flowMessageService;
    private final SubscriptionMessageService subscriptionMessageService; // Внедряем голос подписок

    public SendMessage handle(Long chatId, String input, String state) {
        if (CallbackData.PERIOD_CUSTOM.equals(input)) {
            botSessionService.updateState(chatId, BotState.WAIT_CUSTOM_RENEW);
            return flowMessageService.getAskCustomPeriod(chatId);
        }

        try {
            int days;

            if (BotState.WAIT_CUSTOM_RENEW.equals(state)) {
                days = Integer.parseInt(input);
            } else {
                days = Integer.parseInt(input.replace(CallbackData.PERIOD_PREFIX, ""));
            }

            Long subId = botSessionService.getSession(chatId).getTempSubId();

            subscriptionService.renewSubscription(subId, days);

            botSessionService.updateState(chatId, BotState.IDLE);

            return subscriptionMessageService.getPaymentConfirmedMessage(chatId);

        } catch (Exception e) {
            return commonMessageService.getErrorMessage(chatId, "Введи количество дней числом (напр. 15)");
        }
    }
}