package org.example.subchecker.telegram.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.SubscriptionService;
import org.example.subchecker.service.UserService;
import org.example.subchecker.telegram.messageServiceTg.CommonMessageService;
import org.example.subchecker.telegram.messageServiceTg.SubscriptionFlowMessageService;
import org.example.subchecker.telegram.stateDiary.BotState;
import org.example.subchecker.telegram.stateDiary.CallbackData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddSubscriptionScript {

    private final BotSessionService sessionService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final SubscriptionFlowMessageService flowMessageService;
    private final CommonMessageService commonMessageService;

    public SendMessage handle(Long chatId, String input, String state) {
        switch (state) {
            case BotState.WAIT_NAME:
                sessionService.saveTempName(chatId, input);
                sessionService.updateState(chatId, BotState.WAIT_CATEGORY);
                return flowMessageService.getAskCategory(chatId);

            case BotState.WAIT_CATEGORY:
                sessionService.saveTempCategory(chatId, input.replace(CallbackData.CATEGORY_PREFIX, ""));
                sessionService.updateState(chatId, BotState.WAIT_PERIOD);
                return flowMessageService.getAskPeriod(chatId);

            case BotState.WAIT_PERIOD:
                if (input.equals(CallbackData.PERIOD_CUSTOM)) {
                    sessionService.updateState(chatId, BotState.WAIT_CUSTOM_PERIOD);
                    return flowMessageService.getAskCustomPeriod(chatId);
                }
                sessionService.saveTempPeriod(chatId, Integer.parseInt(input.replace(CallbackData.PERIOD_PREFIX, "")));
                sessionService.updateState(chatId, BotState.WAIT_PRICE);
                return flowMessageService.getAskPrice(chatId, userService.getByTgId(chatId).getDefaultCurrency());

            case BotState.WAIT_CUSTOM_PERIOD:
                try {
                    sessionService.saveTempPeriod(chatId, Integer.parseInt(input));
                    sessionService.updateState(chatId, BotState.WAIT_PRICE);
                    return flowMessageService.getAskPrice(chatId, userService.getByTgId(chatId).getDefaultCurrency());
                } catch (Exception e) {
                    return commonMessageService.getErrorMessage(chatId, "Введи число дней цифрами!");
                }

            case BotState.WAIT_PRICE:
                try {
                    sessionService.saveTempPrice(chatId, Double.parseDouble(input.replace(",", ".")));
                    sessionService.updateState(chatId, BotState.WAIT_URL);
                    return flowMessageService.getAskUrl(chatId);
                } catch (Exception e) {
                    return commonMessageService.getErrorMessage(chatId, "Введи цену числом!");
                }

            case BotState.WAIT_URL:
                String url = (input.contains("Нет ссылки") || input.equalsIgnoreCase("нет")) ? null : input;
                sessionService.saveTempUrl(chatId, url);
                sessionService.updateState(chatId, BotState.WAIT_NOTES);
                return flowMessageService.getAskNotes(chatId);

            case BotState.WAIT_NOTES:
                if (BotState.IDLE.equals(sessionService.getSession(chatId).getState())) return null;
                String notes = (input.contains("Без заметок") || input.equalsIgnoreCase("нет")) ? null : input;
                sessionService.saveTempNotes(chatId, notes);

                // ФИНАЛЬНОЕ СОХРАНЕНИЕ (сработает метод в SubscriptionService)
                subscriptionService.createFromSession(sessionService.getSession(chatId));
                sessionService.updateState(chatId, BotState.IDLE);

                return flowMessageService.getSuccess(chatId);

            default:
                log.warn("Непредвиденный стейт в AddSub: {}", state);
                return commonMessageService.getMainMenuMessage(chatId, "Выберите действие в меню:");
        }
    }
}