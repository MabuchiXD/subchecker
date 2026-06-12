package org.example.subchecker.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.telegram.config.BotConfig;
import org.example.subchecker.telegram.dispatcher.UpdateDispatcher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UpdateDispatcher updateDispatcher;

    @Override
    public String getBotUsername() { return botConfig.getName(); }

    @Override
    public String getBotToken() { return botConfig.getToken(); }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            updateDispatcher.processUpdate(
                    chatId,
                    update.getMessage().getText(),
                    update.getMessage().getFrom().getUserName(),
                    update.getMessage().getFrom().getFirstName()
            ).forEach(this::send);
        }

        else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();

            updateDispatcher.processCallback(chatId, data).forEach(this::send);
        }
    }
    public void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage());
        }
    }
    public void sendSimple(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("Markdown")
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке простого сообщения: {}", e.getMessage());
        }
    }
}