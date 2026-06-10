package org.example.subchecker.telegram.logic;

import org.example.subchecker.dto.SubscriptionDTO;
import org.example.subchecker.model.Currency;
import org.example.subchecker.telegram.botKeyboards.ContextualKeyboards;
import org.example.subchecker.telegram.botKeyboards.PrimaryKeyboards;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SubscriptionFormatter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM");

    public List<SendMessage> formatSummary(Long chatId, List<SubscriptionDTO> subs) {
        if (subs.isEmpty()) {
            return List.of(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text("📓 *Твой список подписок пока пуст.*")
                    .parseMode("Markdown")
                    .replyMarkup(PrimaryKeyboards.mainMenu())
                    .build());
        }

        StringBuilder sb = new StringBuilder("📋 *Твои подписки:*\n\n");
        Map<Currency, Double> totals = new HashMap<>();

        for (int i = 0; i < subs.size(); i++) {
            var sub = subs.get(i);

            // ФОРМАТ: Spotify — 300 RUB (20.05 | 4 дн.)
            String dateStr = sub.getNextPaymentDate().format(DATE_FORMAT);
            sb.append(String.format("%d. %s — *%.2f %s* (%s | %d дн.)\n",
                    i + 1,
                    sub.getServiceName(),
                    sub.getPrice(),
                    sub.getCurrency(),
                    dateStr,
                    sub.getDaysLeft()));

            totals.merge(sub.getCurrency(), sub.getPrice(), Double::sum);
        }

        sb.append("\n──────────────────\n");
        sb.append("💰 *Итого к оплате:*");
        totals.forEach((curr, sum) -> sb.append(String.format("\n— %.2f %s", sum, curr)));
        sb.append("\n\n_Нажми на название ниже для управления:_");

        return List.of(SendMessage.builder()
                .chatId(chatId.toString())
                .text(sb.toString())
                .parseMode("Markdown")
                .replyMarkup(ContextualKeyboards.subscriptionList(subs))
                .build());
    }

    public List<SendMessage> formatSingleSub(Long chatId, SubscriptionDTO sub) {
        boolean isOwner = sub.getOwnerId().equals(chatId);
        String label = sub.isFamily() ? (isOwner ? " [👑 Хост]" : " [👥 Семья]") : "";

        String fullDate = sub.getNextPaymentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        String text = String.format(
                "📦 *%s*%s\n\n" +
                        "💰 Стоимость: *%.2f %s*\n" +
                        "⏳ Оплата: *%s* (через *%d дн.*)\n" + // Добавили дату сюда
                        "──────────────────\n" +
                        "🚀 Бомбер: *%s*\n" +
                        "⏱ Частота: *%d мин*\n" +
                        "📝 Заметки: %s",
                sub.getServiceName().toUpperCase(), label,
                sub.getPrice(), sub.getCurrency(),
                fullDate, sub.getDaysLeft(),
                (sub.getIsHardcore() ? "ВКЛ ✅" : "ВЫКЛ 💤"),
                sub.getBomberIntervalMinutes(),
                (sub.getNotes() != null ? sub.getNotes() : "_нет_")
        );

        return List.of(SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(ContextualKeyboards.subscriptionControl(sub, isOwner))
                .build());
    }
}