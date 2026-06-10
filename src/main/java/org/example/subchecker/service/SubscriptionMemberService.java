package org.example.subchecker.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.subchecker.entity.*;
import org.example.subchecker.exception.ResourceNotFoundException;
import org.example.subchecker.repository.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionMemberService {

    private final SubscriptionMemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional
    public String joinByCode(Long tgId, String code) {
        Subscription sub = subscriptionRepository.findByInviteCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Код не найден или использован"));

        User user = userRepository.findById(tgId).orElseThrow();

        if (memberRepository.findByUserTelegramIdAndSubscriptionId(tgId, sub.getId()).isPresent()) {
            throw new RuntimeException("Ты уже в этой группе!");
        }

        SubscriptionMember member = SubscriptionMember.builder()
                .user(user).subscription(sub)
                .isHardcore(false).bomberIntervalMinutes(60).build();

        memberRepository.save(member);
        return sub.getServiceName();
    }

    @Transactional
    public void kickMember(Long subId, Long targetTgId) {
        memberRepository.findByUserTelegramIdAndSubscriptionId(targetTgId, subId)
                .ifPresent(memberRepository::delete);
    }

    @Transactional
    public void toggleHardcore(Long tgId, Long subId) {
        memberRepository.findByUserTelegramIdAndSubscriptionId(tgId, subId)
                .ifPresent(m -> {
                    m.setIsHardcore(!m.getIsHardcore());
                    memberRepository.save(m);
                });
    }

    @Transactional
    public void updateBomberInterval(Long tgId, Long subId, int minutes) {
        memberRepository.findByUserTelegramIdAndSubscriptionId(tgId, subId)
                .ifPresent(m -> {
                    m.setBomberIntervalMinutes(minutes);
                    memberRepository.save(m);
                });
    }
}