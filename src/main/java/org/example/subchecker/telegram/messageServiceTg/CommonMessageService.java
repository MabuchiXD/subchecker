package org.example.subchecker.telegram.messageServiceTg;

import org.example.subchecker.telegram.botKeyboards.PrimaryKeyboards;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class CommonMessageService {

    public SendMessage getStartMessage(Long chatId) {
        return create(chatId, "Привет! Я твой СДВГ-менеджер подписок. Я помогу тебе ничего не забыть и не потратить лишнего 🚀", PrimaryKeyboards.mainMenu());
    }

    public SendMessage getMainMenuMessage(Long chatId, String text) {
        return create(chatId, text, PrimaryKeyboards.mainMenu());
    }

    public SendMessage getErrorMessage(Long chatId, String errorText) {
        return create(chatId, "⚠️ *Ошибка:* " + errorText, null);
    }

    public SendMessage getGenericMessage(Long chatId, String text) {
        return create(chatId, text, null);
    }

    private SendMessage create(Long chatId, String text, org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard kb) {
        return SendMessage.builder().chatId(chatId.toString()).text(text).replyMarkup(kb).parseMode("Markdown").build();
    }
}