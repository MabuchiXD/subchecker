package org.example.subchecker.telegram.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.core.entity.SubscriptionMember;
import org.example.subchecker.core.mapper.SubscriptionMapper;
import org.example.subchecker.core.repository.SubscriptionMemberRepository;
import org.example.subchecker.telegram.TelegramBot;
import org.example.subchecker.telegram.feature.subscription.SubscriptionFormatter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyNotificationScheduler {

    private final SubscriptionMemberRepository memberRepository;
    private final NotificationService notificationService;
    private final @org.springframework.context.annotation.Lazy TelegramBot telegramBot;
    private final SubscriptionFormatter formatter;
    private final SubscriptionMapper mapper;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void run() {
        log.info("⏳ Запуск вежливых напоминаний...");
        var allMembers = memberRepository.findAll();

        for (SubscriptionMember member : allMembers) {
            if (member.getSubscription().getIsAcknowledged() || !member.getSubscription().getIsActive()) continue;

            if (notificationService.isItTimeToNotify(member.getUser())) {

                if (member.getLastDailyNotifyAt() == null ||
                        Duration.between(member.getLastDailyNotifyAt(), LocalDateTime.now()).toHours() >= 1) {

                    checkDatesAndSend(member);
                }
            }
        }
    }

    private void checkDatesAndSend(SubscriptionMember member) {
        LocalDate today = LocalDate.now();
        LocalDate next = member.getSubscription().getNextPaymentDate();
        String prefix = null;

        if (next.minusDays(1).equals(today)) prefix = "⏳ *ЗАВТРА СПИСАНИЕ!*";
        else if (next.equals(today)) prefix = "‼️ *ОПЛАТА СЕГОДНЯ!*";

        if (prefix != null) {
            var msgs = formatter.formatSingleSub(member.getUser().getTelegramId(), mapper.toDTO(member));
            String finalPrefix = prefix;
            msgs.forEach(m -> {
                m.setText(finalPrefix + "\n\n" + m.getText());
                telegramBot.send(m);
            });
            member.setLastDailyNotifyAt(LocalDateTime.now());
            memberRepository.save(member);
        }
    }
}