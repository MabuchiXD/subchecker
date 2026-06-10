package org.example.subchecker.telegram.logic.handlers.callbacks;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.service.*;
import org.example.subchecker.telegram.messageServiceTg.EditSubscriptionMessageService;
import org.example.subchecker.telegram.stateDiary.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EditCallbackHandler {

    private final BotSessionService sessionService;
    private final SubscriptionService subService;
    private final SubscriptionMemberService memberService;
    private final EditSubscriptionMessageService editMessages;

    public List<SendMessage> handle(Long chatId, String data) {
        // Вход в меню "Изменить"
        if (data.startsWith(CallbackData.EDIT_SUB_MENU_PREFIX)) {
            Long id = extractId(data, CallbackData.EDIT_SUB_MENU_PREFIX);
            var sub = subService.getById(id, chatId);
            return List.of(editMessages.getEditMenu(chatId, id, sub.getServiceName(), sub.getIsHardcore()));
        }

        // Вход в режим правки ТЕКСТОВЫХ полей
        if (data.startsWith(CallbackData.EDIT_FIELD_NAME))  return startFieldEdit(chatId, data, CallbackData.EDIT_FIELD_NAME, BotState.WAIT_EDIT_NAME, "название");
        if (data.startsWith(CallbackData.EDIT_FIELD_PRICE)) return startFieldEdit(chatId, data, CallbackData.EDIT_FIELD_PRICE, BotState.WAIT_EDIT_PRICE, "цену");
        if (data.startsWith(CallbackData.EDIT_FIELD_URL))   return startFieldEdit(chatId, data, CallbackData.EDIT_FIELD_URL, BotState.WAIT_EDIT_URL, "ссылку");
        if (data.startsWith(CallbackData.EDIT_FIELD_NOTES)) return startFieldEdit(chatId, data, CallbackData.EDIT_FIELD_NOTES, BotState.WAIT_EDIT_NOTES, "заметки");

        // Выбор ИНТЕРВАЛА бомбера
        if (data.startsWith(CallbackData.EDIT_FIELD_BOMBER)) {
            return List.of(editMessages.getBomberIntervalMenu(chatId, extractId(data, CallbackData.EDIT_FIELD_BOMBER)));
        }

        if (data.startsWith(CallbackData.SET_BOMBER_INT_PREFIX)) {
            String[] parts = data.replace(CallbackData.SET_BOMBER_INT_PREFIX, "").split("_");
            Long subId = Long.parseLong(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            memberService.updateBomberInterval(chatId, subId, minutes);

            return List.of(editMessages.getBomberIntervalUpdatedMessage(chatId, minutes));
        }

        return null;
    }

    private Long extractId(String data, String prefix) {
        return Long.parseLong(data.substring(prefix.length()));
    }

    private List<SendMessage> startFieldEdit(Long chatId, String data, String prefix, String state, String fieldName) {
        sessionService.saveTempSubId(chatId, extractId(data, prefix));
        sessionService.updateState(chatId, state);
        return List.of(editMessages.getAskNewValue(chatId, fieldName));
    }
}