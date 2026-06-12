package org.example.subchecker.core.repository;

import org.example.subchecker.core.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByOwnerTelegramIdOrderByIdDesc(Long tgId);

    Optional<Subscription> findByInviteCode(String inviteCode);
}