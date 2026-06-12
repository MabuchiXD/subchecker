package org.example.subchecker.web.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.core.entity.User;
import org.example.subchecker.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web/admin") // Общий префикс для админских запросов
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserAdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getAdminStats() {
        log.info("API Admin: Запрос статистики системы");
        long users = adminService.getUsersCount();
        long subs = adminService.getSubscriptionsCount();
        return ResponseEntity.ok(Map.of("usersCount", users, "subsCount", subs));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("API Admin: Запрос всех пользователей");
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long userId, @RequestParam String role) {
        log.info("API Admin: Смена роли пользователя {} на {}", userId, role);
        adminService.updateUserRole(userId, role);
        return ResponseEntity.ok().build();
    }

    // Принудительный сброс сессии
    @PostMapping("/users/{userId}/reset")
    public ResponseEntity<Void> resetUserSession(@PathVariable Long userId) {
        log.info("API Admin: Сброс сессии пользователя {}", userId);
        boolean reset = adminService.resetUserSessionForce(userId);
        return reset ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}