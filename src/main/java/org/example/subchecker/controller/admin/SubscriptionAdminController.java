package org.example.subchecker.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.entity.Subscription;
import org.example.subchecker.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/web/admin/subscriptions") // Изолированный префикс для подписок админа
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SubscriptionAdminController {

    private final AdminService adminService;

    @GetMapping("/all")
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        log.info("API Admin: Запрос всех подписок системы");
        return ResponseEntity.ok(adminService.getAllSubscriptions());
    }

    @DeleteMapping("/{subId}")
    public ResponseEntity<Void> deleteSubscriptionForce(@PathVariable Long subId) {
        log.info("API Admin: Принудительное удаление подписки {}", subId);
        boolean deleted = adminService.deleteSubscriptionForce(subId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}