package org.example.subchecker.telegram.logic.handlers.callbacks;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.model.Currency;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.SubscriptionMemberService;
import org.example.subchecker.service.SubscriptionService;
import org.example.subchecker.telegram.logic.AddSubscriptionScript;
import org.example.subchecker.telegram.logic.RenewSubscriptionScript;
import org.example.subchecker.telegram.logic.SubscriptionFormatter;
import org.example.subchecker.telegram.messageServiceTg.SubscriptionFlowMessageService;
import org.example.subchecker.telegram.messageServiceTg.SubscriptionMessageService;
import org.example.subchecker.telegram.stateDiary.BotState;
import org.example.subchecker.telegram.stateDiary.CallbackData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubCallbackHandler {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMemberService memberService;
    private final SubscriptionFormatter subscriptionFormatter;
    private final SubscriptionMessageService subscriptionMessages;
    private final SubscriptionFlowMessageService flowMessages;
    private final BotSessionService sessionService;
    private final AddSubscriptionScript addSubscriptionScript;
    private final RenewSubscriptionScript renewSubscriptionScript;

    public List<SendMessage> handle(Long chatId, String data) {
        String currentState = sessionService.getSession(chatId).getState();

        // 🟢 1. ИНТЕРЦЕПТОР: Нажатие кнопки "Своя категория" (Используем константы и ставим в самый верх!)
        if (CallbackData.CATEGORY_CUSTOM.equals(data)) {
            sessionService.updateState(chatId, BotState.WAIT_CUSTOM_CATEGORY);
            return List.of(flowMessages.getAskCustomCategory(chatId)); // Делегируем в message-сервис
        }

        // 2. Выбор подписки
        if (data.startsWith(CallbackData.SELECT_PREFIX)) {
            return subscriptionFormatter.formatSingleSub(chatId, subscriptionService.getById(extractId(data, CallbackData.SELECT_PREFIX), chatId));
        }

        // 3. Бомбер
        if (data.startsWith(CallbackData.TOGGLE_BOMBER_PREFIX)) {
            Long id = extractId(data, CallbackData.TOGGLE_BOMBER_PREFIX);
            memberService.toggleHardcore(chatId, id);
            return subscriptionFormatter.formatSingleSub(chatId, subscriptionService.getById(id, chatId));
        }

        // 4. Кнопка продления
        if (data.startsWith(CallbackData.PAY_PREFIX)) {
            Long id = extractId(data, CallbackData.PAY_PREFIX);
            sessionService.saveTempSubId(chatId, id);
            sessionService.updateState(chatId, BotState.WAIT_RENEW_PERIOD);
            return List.of(flowMessages.getAskRenewPeriod(chatId));
        }

        // 5. Кнопка удаления
        if (data.startsWith(CallbackData.DELETE_PREFIX)) {
            Long id = extractId(data, CallbackData.DELETE_PREFIX);
            subscriptionService.deleteSubscription(id);
            return List.of(subscriptionMessages.getDeletedMessage(chatId));
        }

        // 6. Выбор валюты
        if (data.startsWith("SUB_CURR_")) {
            Currency chosenCurr = Currency.valueOf(data.replace("SUB_CURR_", ""));
            sessionService.saveTempCurrency(chatId, chosenCurr);
            return List.of(flowMessages.getAskPrice(chatId, chosenCurr));
        }

        // 7. Общие префиксы категорий и сроков (теперь они не перехватят CATEGORY_CUSTOM)
        if (data.startsWith(CallbackData.CATEGORY_PREFIX) ||
                data.startsWith(CallbackData.PERIOD_PREFIX) ||
                data.equals(CallbackData.PERIOD_CUSTOM)) {

            if (currentState.contains("RENEW")) {
                return List.of(renewSubscriptionScript.handle(chatId, data, currentState));
            } else {
                return List.of(addSubscriptionScript.handle(chatId, data, currentState));
            }
        }

        return null;
    }

    private Long extractId(String data, String prefix) {
        return Long.parseLong(data.substring(prefix.length()));
    }
}