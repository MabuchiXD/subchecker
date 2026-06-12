package org.example.subchecker.web.controller.defaultUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.core.entity.User;
import org.example.subchecker.core.model.Currency;
import org.example.subchecker.service.UserService;
import org.example.subchecker.web.security.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest; // Импортируем
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/web/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserRestController {

    private final UserService userService;
    private final OtpService otpService;

    //госуслуги 2.0
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginByOtp(
            @RequestParam String code,
            HttpServletRequest request) { // 🔴 Принимаем request для считывания IP

        String ip = request.getRemoteAddr();

        //отслеживание по ip
        if (otpService.isIpBlocked(ip)) {
            log.warn("Заблокирован запрос входа по IP: {} за брутфорс", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Превышено количество попыток! Доступ заблокирован на 15 минут."));
        }

        String sessionToken = otpService.validateCodeAndCreateSession(code);

        if (sessionToken == null) {
            otpService.registerFailedAttempt(ip);
            int remaining = otpService.getRemainingAttempts(ip);

            if (remaining == 0) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Неверный код! Доступ заблокирован на 15 минут за брутфорс."));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверный код безопасности! Осталось попыток: " + remaining));
        }

        otpService.clearFailedAttempts(ip);

        Long tgId = Long.valueOf(sessionToken.split(":")[0]);
        String role = userService.getByTgId(tgId).getRole();

        log.info("API: Пользователь {} успешно вошел с IP {}! Создана сессия {}", tgId, ip, sessionToken);

        return ResponseEntity.ok(Map.of(
                "sessionToken", sessionToken,
                "role", role
        ));
    }

    // Получить профиль текущего пользователя (/me)
    @GetMapping("/me")
    public ResponseEntity<User> getMe(@RequestAttribute("validatedTgId") Long tgId) {
        log.info("API: Запрос профиля /me для пользователя {}", tgId);
        return ResponseEntity.ok(userService.getByTgId(tgId));
    }

    // Получить роль текущего пользователя
    @GetMapping("/role")
    public ResponseEntity<Map<String, String>> getUserRole(@RequestAttribute("validatedTgId") Long tgId) {
        try {
            String role = userService.getByTgId(tgId).getRole();
            return ResponseEntity.ok(Map.of("role", role));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("role", "USER"));
        }
    }

    // Получить личные настройки юзера
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getUserSettings(@RequestAttribute("validatedTgId") Long tgId) {
        User user = userService.getByTgId(tgId);
        return ResponseEntity.ok(Map.of(
                "preferredTime", user.getPreferredNotificationTime().toString().substring(0, 5),
                "timezoneOffset", user.getTimezoneOffset(),
                "defaultCurrency", user.getDefaultCurrency().name()
        ));
    }

    // Сохранить личные настройки юзера
    @PutMapping("/settings")
    public ResponseEntity<Void> updateUserSettings(
            @RequestAttribute("validatedTgId") Long tgId,
            @RequestBody Map<String, Object> payload) {

        log.info("API: Обновление настроек юзера {}", tgId);

        if (payload.containsKey("preferredTime")) {
            LocalTime time = LocalTime.parse((String) payload.get("preferredTime"));
            userService.updatePreferredTime(tgId, time);
        }
        if (payload.containsKey("timezoneOffset")) {
            int offset = Integer.parseInt(payload.get("timezoneOffset").toString());
            userService.updateTimezone(tgId, offset);
        }
        if (payload.containsKey("defaultCurrency")) {
            Currency currency = Currency.valueOf((String) payload.get("defaultCurrency"));
            userService.updateDefaultCurrency(tgId, currency);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAllDevices(@RequestAttribute("validatedTgId") Long tgId) {
        otpService.logoutAllDevices(tgId);
        return ResponseEntity.ok().build();
    }

}