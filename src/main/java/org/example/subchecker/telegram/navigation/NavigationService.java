package org.example.subchecker.telegram.navigation;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.UserService;
import org.example.subchecker.telegram.messageServiceTg.CommonMessageService;
import org.example.subchecker.telegram.messageServiceTg.SubscriptionFlowMessageService;
import org.example.subchecker.telegram.stateDiary.BotState;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
public class NavigationService {

    private final BotSessionService sessionService;
    private final UserService userService;
    private final CommonMessageService commonMessageService;
    private final SubscriptionFlowMessageService flowMessageService;

    public SendMessage handleBack(Long chatId, String currentState) {
        String prevState;
        SendMessage response;

        switch (currentState) {
            case BotState.WAIT_CATEGORY:
                prevState = BotState.IDLE;
                response = commonMessageService.getMainMenuMessage(chatId, "Возврат в меню:");
                break;
            case BotState.WAIT_PERIOD:
                prevState = BotState.WAIT_NAME;
                response = flowMessageService.getAskName(chatId);
                break;
            case BotState.WAIT_PRICE:
                prevState = BotState.WAIT_CATEGORY;
                response = flowMessageService.getAskCategory(chatId);
                break;
            case BotState.WAIT_URL:
                prevState = BotState.WAIT_PERIOD;
                response = flowMessageService.getAskPeriod(chatId);
                break;
            case BotState.WAIT_NOTES:
                prevState = BotState.WAIT_PRICE;
                response = flowMessageService.getAskPrice(chatId, userService.getByTgId(chatId).getDefaultCurrency());
                break;
            case BotState.WAIT_INVITE_CODE:
            case BotState.WAIT_RENEW_PERIOD:
                prevState = BotState.IDLE;
                response = commonMessageService.getMainMenuMessage(chatId, "Действие отменено.");
                break;
            default:
                prevState = BotState.IDLE;
                response = commonMessageService.getMainMenuMessage(chatId, "Главное меню:");
        }

        sessionService.updateState(chatId, prevState);
        return response;
    }
}