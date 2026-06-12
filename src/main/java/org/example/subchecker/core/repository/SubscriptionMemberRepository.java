package org.example.subchecker.core.repository;

import org.example.subchecker.core.entity.SubscriptionMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionMemberRepository extends JpaRepository<SubscriptionMember, Long> {
    List<SubscriptionMember> findAllByUserTelegramId(Long telegramId);

    List<SubscriptionMember> findAllByIsHardcoreTrue();

    List<SubscriptionMember> findAll();

    Optional<SubscriptionMember> findByUserTelegramIdAndSubscriptionId(Long telegramId, Long subscriptionId);
}