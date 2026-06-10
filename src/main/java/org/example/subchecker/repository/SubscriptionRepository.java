package org.example.subchecker.repository;

import org.example.subchecker.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByOwnerTelegramIdOrderByIdDesc(Long tgId);

    Optional<Subscription> findByInviteCode(String inviteCode);
}