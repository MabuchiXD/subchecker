package org.example.subchecker.telegram.messageServiceTg;

import org.example.subchecker.telegram.botKeyboards.ContextualKeyboards;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class EditSubscriptionMessageService {


    public SendMessage getEditMenu(Long chatId, Long subId, String serviceName, boolean isHardcore) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text("⚙️ *Редактирование:* " + serviceName + "\n_Что именно хочешь изменить?_")
                .replyMarkup(ContextualKeyboards.editSubscriptionMenu(subId, isHardcore))
                .parseMode("Markdown")
                .build();
    }

    public SendMessage getBomberIntervalMenu(Long chatId, Long subId) {
        String text = "⏱ *Настройка уведомлений*\n\n" +
                "Как часто бот должен присылать напоминания, если день оплаты наступил, а ты ещё не нажал кнопку «✅ Оплатил»?\n\n" +
                "_Как только ты подтвердишь оплату, бот сразу замолчит до следующего периода._";

        return create(chatId, text, ContextualKeyboards.bomberIntervalSelection(subId));
    }

    public SendMessage getAskNewValue(Long chatId, String fieldName) {
        return SendMessage.builder().chatId(chatId.toString())
                .text("📝 Введи новое значение для поля: *" + fieldName + "*\n(или нажми Назад)")
                .replyMarkup(ContextualKeyboards.justBackKeyboard()).parseMode("Markdown").build();
    }
    public SendMessage getBomberIntervalUpdatedMessage(Long chatId, int minutes) {
        return create(chatId, "✅ *Частота Бомбера изменена!*\nТеперь я буду напоминать тебе каждые " + minutes + " мин.", null);
    }
    private SendMessage create(Long chatId, String text, org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard kb) {
        return SendMessage.builder().chatId(chatId.toString()).text(text).replyMarkup(kb).parseMode("Markdown").build();
    }

}