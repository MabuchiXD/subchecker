package org.example.subchecker.telegram.logic;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.service.BotSessionService;
import org.example.subchecker.service.SubscriptionEditService;
import org.example.subchecker.service.SubscriptionMemberService;
import org.example.subchecker.telegram.messageServiceTg.CommonMessageService;
import org.example.subchecker.telegram.stateDiary.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class EditSubscriptionScript {

    private final SubscriptionEditService editService;
    private final SubscriptionMemberService memberService; // Для изменения интервала Бомбера
    private final BotSessionService sessionService;
    private final CommonMessageService commonMessageService;

    public SendMessage handle(Long chatId, String input, String state) {
        Long subId = sessionService.getSession(chatId).getTempSubId();

        try {
            switch (state) {
                case BotState.WAIT_EDIT_NAME:
                    editService.updateName(subId, input);
                    break;

                case BotState.WAIT_EDIT_PRICE:
                    editService.updatePrice(subId, Double.parseDouble(input.replace(",", ".")));
                    break;

                case BotState.WAIT_EDIT_URL:
                    String url = input.equalsIgnoreCase("нет") ? null : input;
                    editService.updateUrl(subId, url);
                    break;

                case BotState.WAIT_EDIT_NOTES:
                    String notes = input.equalsIgnoreCase("нет") ? null : input;
                    editService.updateNotes(subId, notes);
                    break;

                case BotState.WAIT_EDIT_BOMBER_INT:
                    memberService.updateBomberInterval(chatId, subId, Integer.parseInt(input));
                    break;
            }

            sessionService.updateState(chatId, BotState.IDLE);
            return commonMessageService.getGenericMessage(chatId, "✅ Изменения успешно сохранены!");

        } catch (Exception e) {
            return commonMessageService.getErrorMessage(chatId, "❌ Ошибка формата! Попробуй еще раз.");
        }
    }
}