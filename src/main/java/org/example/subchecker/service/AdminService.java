package org.example.subchecker.service;

import lombok.RequiredArgsConstructor;
import org.example.subchecker.core.entity.Subscription;
import org.example.subchecker.core.entity.User;
import org.example.subchecker.core.repository.BotSessionRepository;
import org.example.subchecker.core.repository.SubscriptionRepository;
import org.example.subchecker.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BotSessionRepository botSessionRepository;

    public long getUsersCount() {
        return userRepository.count();
    }

    public long getSubscriptionsCount() {
        return subscriptionRepository.count();
    }

    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public boolean deleteSubscriptionForce(Long subId) {
        if (subscriptionRepository.existsById(subId)) {
            subscriptionRepository.deleteById(subId);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean resetUserSessionForce(Long userId) {
        return botSessionRepository.findById(userId).map(session -> {
            session.setState("IDLE");
            session.setTempName(null);
            session.setTempPrice(null);
            session.setTempUrl(null);
            session.setTempCategory(null);
            session.setTempPeriodDays(null);
            session.setTempSubId(null);
            session.setTempNotes(null);
            session.setTempCurrency(null);
            session.setTempPrefTime(null);
            session.setTempTimezoneOffset(null);
            session.setTempDefaultCurrency(null);
            botSessionRepository.save(session);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void updateUserRole(Long userId, String role) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
    }
}