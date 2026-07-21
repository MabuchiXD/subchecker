package org.example.subchecker.telegram.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.core.entity.SubscriptionMember;
import org.example.subchecker.core.repository.SubscriptionMemberRepository;
import org.example.subchecker.telegram.TelegramBot;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BomberScheduler {

    private final SubscriptionMemberRepository memberRepository;
    private final @org.springframework.context.annotation.Lazy TelegramBot telegramBot;

    @Scheduled(cron = "0 0/15 * * * *") // Проверка каждые 15 минут
    @Transactional
    public void run() {
        log.info("💀 БОМБЕР на охоте...");
        var targets = memberRepository.findAllByIsHardcoreTrue();

        for (SubscriptionMember member : targets) {
            var sub = member.getSubscription();

            // Рассчитываем сегодняшнее число с учетом часового пояса текущего пользователя
            ZonedDateTime userNow = ZonedDateTime.now(ZoneOffset.UTC).plusHours(member.getUser().getTimezoneOffset());
            LocalDate today = userNow.toLocalDate();

            if (!sub.getNextPaymentDate().isAfter(today) && !sub.getIsAcknowledged() && sub.getIsActive()) {
                if (shouldBomb(member)) {
                    telegramBot.sendSimple(member.getUser().getTelegramId(),
                            "💀 *БОМБЕР:* Ты забыл оплатить *" + sub.getServiceName() + "*!");

                    member.setLastBomberNotifySent(LocalDateTime.now());
                    memberRepository.save(member);
                    log.info("💥 Бомбер сработал для пользователя {}", member.getUser().getTelegramId());
                }
            }
        }
    }

    private boolean shouldBomb(SubscriptionMember member) {
        if (member.getLastBomberNotifySent() == null) return true;
        return LocalDateTime.now().isAfter(
                member.getLastBomberNotifySent().plusMinutes(member.getBomberIntervalMinutes())
        );
    }
}