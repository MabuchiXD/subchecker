package org.example.subchecker.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.entity.SubscriptionMember;
import org.example.subchecker.repository.SubscriptionMemberRepository;
import org.example.subchecker.telegram.TelegramBot;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BomberScheduler {

    private final SubscriptionMemberRepository memberRepository;
    private final @org.springframework.context.annotation.Lazy TelegramBot telegramBot;

    @Scheduled(cron = "0 0/15 * * * *") // Каждые 15 минут
    @Transactional
    public void run() {
        log.info("💀 БОМБЕР на охоте...");
        var targets = memberRepository.findAllByIsHardcoreTrue();
        LocalDate today = LocalDate.now();

        for (SubscriptionMember member : targets) {
            var sub = member.getSubscription();

            if (sub.getNextPaymentDate().isBefore(today) && !sub.getIsAcknowledged()) {
                if (shouldBomb(member)) {
                    telegramBot.sendSimple(member.getUser().getTelegramId(),
                            "💀 *БОМБЕР:* Ты забыл оплатить *" + sub.getServiceName() + "*!");

                    member.setLastBomberNotifySent(LocalDateTime.now());
                    memberRepository.save(member);
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