package org.example.subchecker.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Getter
    @Value("${telegram.bot.token}")
    private String botToken;

    private static final String HMAC_SHA256 = "HmacSHA256";

    // Хранилище одноразовых кодов, живущих 5 минут
    private final Map<String, OtpData> otpMap = new ConcurrentHashMap<>();

    private final Map<String, IpBlockData> ipBlockMap = new ConcurrentHashMap<>();

    // ГЕНЕРАЦИЯ КОДА В БОТЕ
    public String generateCode(Long tgId) {
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        otpMap.put(code, new OtpData(tgId, LocalDateTime.now().plusMinutes(5)));
        return code;
    }

    // ВАЛИДАЦИЯ КОДА И СОЗДАНИЕ СЕССИИ
    public String validateCodeAndCreateSession(String code) {
        OtpData data = otpMap.get(code);

        if (data == null) {
            return null;
        }

        otpMap.remove(code);

        if (LocalDateTime.now().isAfter(data.expiryTime)) {
            return null;
        }

        Long tgId = data.telegramId;
        String signature = calculateHmac(tgId.toString(), botToken);

        return tgId + ":" + signature;
    }

    // ПРОВЕРКА БЛОКИРОВКИ IP-АДРЕСА
    public boolean isIpBlocked(String ip) {
        IpBlockData data = ipBlockMap.get(ip);
        if (data == null) return false;

        if (data.blockedUntil != null && LocalDateTime.now().isBefore(data.blockedUntil)) {
            return true; // IP все еще заблокирован
        }

        if (data.blockedUntil != null && LocalDateTime.now().isAfter(data.blockedUntil)) {
            // Время блокировки истекло — очищаем память
            ipBlockMap.remove(ip);
            return false;
        }

        return false;
    }

    // ПОЛУЧИТЬ ОСТАВШИЕСЯ ПОПЫТКИ
    public int getRemainingAttempts(String ip) {
        IpBlockData data = ipBlockMap.get(ip);
        if (data == null) return 3; // По умолчанию даем 3 попытки
        return Math.max(0, 3 - data.attempts);
    }

    // РЕГИСТРАЦИЯ НЕУДАЧНОЙ ПОПЫТКИ ВХОДА
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