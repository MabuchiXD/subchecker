package org.example.subchecker.telegram.messageServiceTg;

import org.example.subchecker.model.Currency;
import org.example.subchecker.telegram.botKeyboards.ContextualKeyboards;
import org.example.subchecker.telegram.botKeyboards.PrimaryKeyboards;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class SubscriptionFlowMessageService {

    public SendMessage getAskName(Long chatId) {
        return create(chatId, "📝 *Введи название сервиса* (напр. Netflix):", ContextualKeyboards.justBackKeyboard());
    }

    public SendMessage getAskCategory(Long chatId) {
        return create(chatId, "🎬 *Выбери категорию* или напиши свою:", ContextualKeyboards.categorySelection());
    }

    public SendMessage getAskCustomCategory(Long chatId) {
        return create(chatId, "✏️ *Введите название своей категории:*", ContextualKeyboards.justBackKeyboard());
    }

    public SendMessage getAskPeriod(Long chatId) {
        return create(chatId, "📅 *На какой срок оформлена подписка?*", ContextualKeyboards.periodSelection());
    }

    public SendMessage getAskPrice(Long chatId, Currency currentCurrency) {
        String currName = (currentCurrency != null) ? currentCurrency.name() : "RUB";

        String text = "💰 *Введите стоимость подписки:*\n\n" +
                "Текущая валюта: *" + currName + "*\n" +
                "_Если нужно, смени валюту кнопкой ниже и напиши цену цифрами_";

        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(ContextualKeyboards.priceCurrencyButtons())
                .build();
    }

    // ВОТ ОНИ - КНОПКИ СКИПА URL
    public SendMessage getAskUrl(Long chatId) {
        return create(chatId, "🔗 *Пришли ссылку на оплату* (чтобы не искать её потом):",
                ContextualKeyboards.skipUrlKeyboard());
    }

    // ВОТ ОНИ - КНОПКИ СКИПА ЗАМЕТОК
    public SendMessage getAskNotes(Long chatId) {
        return create(chatId, "📝 *Есть какие-то заметки?* (данные аккаунта и т.д.)\nНапиши текстом или нажми кнопку:",
                ContextualKeyboards.skipNotesKeyboard());
    }

    public SendMessage getAskCustomPeriod(Long chatId) {
        return create(chatId, "✏️ *Введи количество дней подписки* (только число):", ContextualKeyboards.justBackKeyboard());
    }

    public SendMessage getAskRenewPeriod(Long chatId) {
        return create(chatId, "🔄 *На какой срок ты продлил подписку?*", ContextualKeyboards.periodSelection());
    }

    public SendMessage getAskInviteCode(Long chatId) {
        return create(chatId, "📩 *Вступление в группу*\n\nВведи секретный код от друга:", ContextualKeyboards.justBackKeyboard());
    }

    public SendMessage getSuccess(Long chatId) {
        return create(chatId, "✅ *Подписка добавлена!* Теперь я за ней слежу.", PrimaryKeyboards.mainMenu());
    }

    public SendMessage getInviteSuccess(Long chatId, String serviceName) {
        return create(chatId, "✅ *Готово!* Ты вступил в группу *" + serviceName + "*", PrimaryKeyboards.mainMenu());
    }

    private SendMessage create(Long chatId, String text, org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard kb) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(kb)
                .parseMode("Markdown")
                .build();
    }
}