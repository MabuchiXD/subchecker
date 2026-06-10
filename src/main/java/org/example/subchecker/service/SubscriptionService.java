package org.example.subchecker.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.dto.SubscriptionDTO;
import org.example.subchecker.dto.UserDTO;
import org.example.subchecker.entity.*;
import org.example.subchecker.exception.ResourceNotFoundException;
import org.example.subchecker.mapper.SubscriptionMapper;
import org.example.subchecker.mapper.UserMapper;
import org.example.subchecker.model.Currency;
import org.example.subchecker.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMemberRepository memberRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserMapper userMapper;

    @Transactional
    public List<SubscriptionDTO> getMySubscriptions(Long tgId) {
        return memberRepository.findAllByUserTelegramId(tgId).stream()
                .map(subscriptionMapper::toDTO)
                // 🟢 ИСПРАВЛЕНО: Делаем сортировку Null-Safe (безопасной к пустым дням окончания)
                .sorted(Comparator.comparing(
                        SubscriptionDTO::getDaysLeft,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionDTO getById(Long subId, Long tgId) {
        return memberRepository.findByUserTelegramIdAndSubscriptionId(tgId, subId)
                .map(subscriptionMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Доступ запрещен или подписка не найдена"));
    }

    @Transactional
    public void createFromSession(BotSession session) {
        User owner = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        subscriptionRepository.findTopByOwnerTelegramIdOrderByIdDesc(owner.getTelegramId())
                .ifPresent(lastSub -> {
                    if (lastSub.getServiceName().equals(session.getTempName())) {
                        log.warn("⛔ Попытка двойного сохранения {}. Игнорирую.", session.getTempName());
                    }
                });

        Currency finalCurrency = (session.getTempCurrency() != null)
                ? session.getTempCurrency()
                : owner.getDefaultCurrency();

        int days = (session.getTempPeriodDays() != null) ? session.getTempPeriodDays() : 30;

        Subscription sub = Subscription.builder()
                .serviceName(session.getTempName())
                .category(session.getTempCategory())
                .price(session.getTempPrice())
                .currency(finalCurrency)
                .paymentUrl(session.getTempUrl())
                .notes(session.getTempNotes())
                .owner(owner)
                .periodDays(days)
                .lastPaymentDate(LocalDate.now())
                .nextPaymentDate(LocalDate.now().plusDays(days))
                .isActive(true)
                .isAcknowledged(false)
                .build();

        Subscription savedSub = subscriptionRepository.save(sub);

        SubscriptionMember member = SubscriptionMember.builder()
                .user(owner)
                .subscription(savedSub)
                .isHardcore(false)
                .bomberIntervalMinutes(60)
                .build();

        memberRepository.save(member);

        log.info("✅ Подписка '{}' и Member созданы для {}", savedSub.getServiceName(), owner.getTelegramId());
    }

    @Transactional
    public void deleteSubscription(Long subId) {
        subscriptionRepository.findById(subId).ifPresent(subscriptionRepository::delete);
        log.info("🗑 Подписка {} полностью стерта", subId);
    }

    @Transactional
    public void renewSubscription(Long subId, int days) {
        subscriptionRepository.findById(subId).ifPresent(sub -> {
            sub.renew(days);
            subscriptionRepository.save(sub);
            log.info("🔄 Подписка {} продлена через метод Entity", sub.getServiceName());
        });
    }

    @Transactional
    public String generateInviteCode(Long subId) {
        String code = "INV_" + java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        subscriptionRepository.findById(subId).ifPresent(sub -> {
            sub.setInviteCode(code);
            subscriptionRepository.save(sub);
        });
        return code;
    }

    @Transactional
    public List<UserDTO> getSubscriptionMembers(Long subId) {
        Subscription sub = subscriptionRepository.findById(subId).orElse(null);
        if (sub == null || sub.getMembers() == null) {
            return new java.util.ArrayList<>();
        }

        return sub.getMembers().stream()
                .map(member -> userMapper.toDTO(member.getUser()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeInviteCode(Long subId) {
        subscriptionRepository.findById(subId).ifPresent(sub -> {
            sub.setInviteCode(null);
            subscriptionRepository.save(sub);
            log.info("Инвайт-код для подписки {} аннулирован", subId);
        });
    }
}