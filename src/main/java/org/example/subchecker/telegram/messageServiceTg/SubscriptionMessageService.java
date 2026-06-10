package org.example.subchecker.telegram.messageServiceTg;

import org.example.subchecker.telegram.botKeyboards.PrimaryKeyboards;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class SubscriptionMessageService {

    public SendMessage getDeletedMessage(Long chatId) {
        return create(chatId, "🗑 *Подписка удалена!*", PrimaryKeyboards.mainMenu());
    }

    public SendMessage getPaymentConfirmedMessage(Long chatId) {
        return create(chatId, "✅ Оплата зафиксирована!", null);
    }

    private SendMessage create(Long chatId, String text, org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard kb) {
        return SendMessage.builder().chatId(chatId.toString()).text(text).replyMarkup(kb).parseMode("Markdown").build();
    }
}