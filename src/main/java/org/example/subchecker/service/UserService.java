package org.example.subchecker.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.entity.User;
import org.example.subchecker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public User getByTgId(Long tgId) {
        return userRepository.findById(tgId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + tgId));
    }

    @Transactional
    public User registerOrUpdateUser(Long tgId, String username, String firstName) {
        return userRepository.findById(tgId).map(existingUser -> {
            existingUser.setUsername(username);
            existingUser.setFirstName(firstName);
            return userRepository.save(existingUser);
        }).orElseGet(() -> {
            User newUser = User.builder()
                    .telegramId(tgId)
                    .username(username)
                    .firstName(firstName)
                    .role("USER")
                    .preferredNotificationTime(LocalTime.of(10, 0))
                    .timezoneOffset(3)
                    .build();
            return userRepository.save(newUser);
        });
    }
    @Transactional
    public void updatePreferredTime(Long tgId, java.time.LocalTime time) {
        userRepository.findById(tgId).ifPresent(user -> {
            user.setPreferredNotificationTime(time);
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateTimezone(Long tgId, int offset) {
        userRepository.findById(tgId).ifPresent(user -> {
            user.setTimezoneOffset(offset);
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateDefaultCurrency(Long tgId, org.example.subchecker.model.Currency currency) {
        userRepository.findById(tgId).ifPresent(user -> {
            user.setDefaultCurrency(currency);
            userRepository.save(user);
        });
    }

    @Transactional
    public void saveSettingsFromSession(Long tgId, org.example.subchecker.entity.BotSession session) {
        if (session.getTempPrefTime() != null) {
            updatePreferredTime(tgId, session.getTempPrefTime());
        }
        if (session.getTempTimezoneOffset() != null) {
            updateTimezone(tgId, session.getTempTimezoneOffset());
        }
        if (session.getTempDefaultCurrency() != null) {
            updateDefaultCurrency(tgId, session.getTempDefaultCurrency());
        }

        log.info("✅ Настройки профиля для {} успешно зафиксированы", tgId);
    }
}