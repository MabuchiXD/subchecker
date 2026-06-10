package org.example.subchecker.mapper;

import org.example.subchecker.dto.SubscriptionDTO;
import org.example.subchecker.entity.Subscription;
import org.example.subchecker.entity.SubscriptionMember;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class SubscriptionMapper {

    public SubscriptionDTO toDTO(SubscriptionMember member) {
        Subscription sub = member.getSubscription();

        return SubscriptionDTO.builder()
                .id(sub.getId())
                .serviceName(sub.getServiceName())
                .category(sub.getCategory())
                .price(sub.getPrice())
                .currency(sub.getCurrency())
                .nextPaymentDate(sub.getNextPaymentDate())
                .periodDays(sub.getPeriodDays())
                .paymentUrl(sub.getPaymentUrl())
                .notes(sub.getNotes())
                .isAcknowledged(sub.getIsAcknowledged())
                .ownerId(sub.getOwner().getTelegramId())
                .inviteCode(sub.getInviteCode())
                .isFamily(sub.getMembers() != null && sub.getMembers().size() > 1)
                .daysLeft(calculateDaysLeft(sub.getNextPaymentDate()))
                .isHardcore(member.getIsHardcore())
                .bomberIntervalMinutes(member.getBomberIntervalMinutes())
                .build();
    }

    private Long calculateDaysLeft(LocalDate nextPaymentDate) {
        if (nextPaymentDate == null) return 0L;
        return ChronoUnit.DAYS.between(LocalDate.now(), nextPaymentDate);
    }
}