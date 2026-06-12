package org.example.subchecker.web.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.subchecker.core.entity.User;
import org.example.subchecker.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final UserRepository userRepository;

    @Getter
    @Value("${telegram.bot.token}")
    private String botToken;

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final Map<String, OtpData> otpMap = new ConcurrentHashMap<>();
    private final Map<String, IpBlockData> ipBlockMap = new ConcurrentHashMap<>();

    public String generateCode(Long tgId) {
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        otpMap.put(code, new OtpData(tgId, LocalDateTime.now().plusMinutes(1)));
        return code;
    }

    // ВАЛИДАЦИЯ С УЧЕТОМ ВЕРСИИ ТОКЕНА
    @Transactional
    public String validateCodeAndCreateSession(String code) {
        OtpData data = otpMap.get(code);
        if (data == null) return null;

        otpMap.remove(code);
        if (LocalDateTime.now().isAfter(data.expiryTime)) return null;

        Long tgId = data.telegramId;
        User user = userRepository.findById(tgId).orElse(null);
        if (user == null) return null;

        // Включаем token_version в подпись
        String signature = calculateHmac(tgId + ":" + user.getTokenVersion(), botToken);
        return tgId + ":" + signature;
    }

    // ПРОВЕРКА ТОКЕНА ИЗ ФИЛЬТРА С УЧЕТОМ ВЕРСИИ
    public boolean validateSessionToken(Long userId, String signature) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        String expectedSignature = calculateHmac(userId + ":" + user.getTokenVersion(), botToken);
        return expectedSignature.equalsIgnoreCase(signature);
    }

    // СБРОС ВСЕХ СЕССИЙ (Увеличение версии токена)
    @Transactional
    public void logoutAllDevices(Long tgId) {
        userRepository.findById(tgId).ifPresent(user -> {
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        });
    }

    public boolean isIpBlocked(String ip) {
        IpBlockData data = ipBlockMap.get(ip);
        if (data == null) return false;
        if (data.blockedUntil != null && LocalDateTime.now().isBefore(data.blockedUntil)) return true;
        if (data.blockedUntil != null && LocalDateTime.now().isAfter(data.blockedUntil)) {
            ipBlockMap.remove(ip);
            return false;
        }
        return false;
    }

    public int getRemainingAttempts(String ip) {
        IpBlockData data = ipBlockMap.get(ip);
        if (data == null) return 3;
        return Math.max(0, 3 - data.attempts);
    }

    public void registerFailedAttempt(String ip) {
        IpBlockData data = ipBlockMap.get(ip);
        if (data == null) {
            ipBlockMap.put(ip, new IpBlockData(1, null));
        } else {
            data.attempts++;
            if (data.attempts >= 3) {
                data.blockedUntil = LocalDateTime.now().plusMinutes(15);
            }
        }
    }

    public void clearFailedAttempts(String ip) {
        ipBlockMap.remove(ip);
    }

    public String calculateHmac(String data, String key) {
        try {
            Mac hmac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            hmac.init(secretKeySpec);
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования токена", e);
        }
    }

    private static class OtpData {
        final Long telegramId;
        final LocalDateTime expiryTime;
        OtpData(Long telegramId, LocalDateTime expiryTime) {
            this.telegramId = telegramId;
            this.expiryTime = expiryTime;
        }
    }

    private static class IpBlockData {
        int attempts;
        LocalDateTime blockedUntil;
        IpBlockData(int attempts, LocalDateTime blockedUntil) {
            this.attempts = attempts;
            this.blockedUntil = blockedUntil;
        }
    }
}