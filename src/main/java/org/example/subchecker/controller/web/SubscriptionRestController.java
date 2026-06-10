package org.example.subchecker.controller.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.dto.SubscriptionDTO;
import org.example.subchecker.entity.Subscription;
import org.example.subchecker.entity.SubscriptionMember;
import org.example.subchecker.entity.User;
import org.example.subchecker.service.SubscriptionEditService;
import org.example.subchecker.service.SubscriptionMemberService;
import org.example.subchecker.service.SubscriptionService;
import org.example.subchecker.service.UserService;
import org.example.subchecker.repository.SubscriptionMemberRepository;
import org.example.subchecker.repository.SubscriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web/subscriptions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SubscriptionRestController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionEditService subscriptionEditService;
    private final SubscriptionMemberService subscriptionMemberService;
    private final UserService userService;
    private final SubscriptionMemberRepository subscriptionMemberRepository;
    private final SubscriptionRepository subscriptionRepository;

    @GetMapping
    public ResponseEntity<List<SubscriptionDTO>> getUserSubscriptions(@RequestAttribute("validatedTgId") Long tgId) {
        log.info("API: Запрос подписок для пользователя ID: {}", tgId);
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(tgId));
    }

    //ваще это частично админский метод, но мне лень реформатить сейчас
    @GetMapping("/user/{targetTgId}")
    public ResponseEntity<List<SubscriptionDTO>> getSubscriptionsByUserId(@PathVariable Long targetTgId) {
        log.info("API Admin: Запрос подписок для целевого пользователя ID: {}", targetTgId);
        return ResponseEntity.ok(subscriptionService.getMySubscriptions(targetTgId));
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinByCode(
            @RequestParam String code,
            @RequestAttribute("validatedTgId") Long tgId) {
        log.info("API: Попытка вступления пользователя {} по коду {}", tgId, code);
        try {
            String serviceName = subscriptionMemberService.joinByCode(tgId, code);
            return ResponseEntity.ok(Map.of("serviceName", serviceName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createSubscription(
            @RequestAttribute("validatedTgId") Long tgId,
            @RequestBody Map<String, Object> payload) {

        log.info("API: Создание новой подписки с сайта для юзера {}", tgId);

        User owner = userService.getByTgId(tgId);
        int days = payload.containsKey("periodDays") ? Integer.parseInt(payload.get("periodDays").toString()) : 30;

        // Считываем ручную дату окончания, если она передана с сайта
        LocalDate nextDate;
        if (payload.containsKey("nextPaymentDate") && payload.get("nextPaymentDate") != null) {
            nextDate = LocalDate.parse((String) payload.get("nextPaymentDate"));
        } else {
            nextDate = LocalDate.now().plusDays(days);
        }

        Subscription sub = Subscription.builder()
                .serviceName((String) payload.get("serviceName"))
                .category((String) payload.get("category"))
                .price(Double.valueOf(payload.get("price").toString()))
                .currency(org.example.subchecker.model.Currency.valueOf(payload.getOrDefault("currency", "RUB").toString()))
                .paymentUrl((String) payload.get("paymentUrl"))
                .notes((String) payload.get("notes"))
                .owner(owner)
                .periodDays(days)
                .lastPaymentDate(LocalDate.now())
                .nextPaymentDate(nextDate) // Записываем дату
                .isActive(true)
                .isAcknowledged(false)
                .build();

        Subscription savedSub = subscriptionRepository.save(sub);

        SubscriptionMember member = SubscriptionMember.builder()
                .user(owner)
                .subscription(savedSub)
                .isHardcore(false)
                .bomberIntervalMinutes(60)
                .build();

        subscriptionMemberRepository.save(member);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{subId}/renew")
    public ResponseEntity<Void> renewSubscription(@PathVariable Long subId, @RequestParam int days) {
        subscriptionService.renewSubscription(subId, days);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{subId}/invite")
    public ResponseEntity<Map<String, String>> generateInvite(@PathVariable Long subId) {
        String code = subscriptionService.generateInviteCode(subId);
        return ResponseEntity.ok(Map.of("code", code));
    }

    @DeleteMapping("/{subId}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long subId) {
        subscriptionService.deleteSubscription(subId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{subId}/edit")
    public ResponseEntity<Void> editSubscription(
            @PathVariable Long subId,
            @RequestAttribute("validatedTgId") Long tgId,
            @RequestBody Map<String, Object> payload) {

        log.info("API: Редактирование подписки ID {}", subId);

        //поля информации о подписке
        if (payload.containsKey("serviceName")) {
            subscriptionEditService.updateName(subId, (String) payload.get("serviceName"));
        }
        if (payload.containsKey("price")) {
            Double price = Double.valueOf(payload.get("price").toString());
            subscriptionEditService.updatePrice(subId, price);
        }
        if (payload.containsKey("notes")) {
            subscriptionEditService.updateNotes(subId, (String) payload.get("notes"));
        }
        if (payload.containsKey("paymentUrl")) {
            subscriptionEditService.updateUrl(subId, (String) payload.get("paymentUrl"));
        }

        subscriptionRepository.findById(subId).ifPresent(sub -> {
            if (payload.containsKey("currency")) {
                org.example.subchecker.model.Currency currency = org.example.subchecker.model.Currency.valueOf((String) payload.get("currency"));
                sub.setCurrency(currency);
            }
            if (payload.containsKey("nextPaymentDate") && payload.get("nextPaymentDate") != null) {
                LocalDate nextDate = LocalDate.parse((String) payload.get("nextPaymentDate"));
                sub.setNextPaymentDate(nextDate);
            }
            subscriptionRepository.save(sub);
        });

        //бомбер
        subscriptionMemberRepository.findByUserTelegramIdAndSubscriptionId(tgId, subId).ifPresent(member -> {
            if (payload.containsKey("isHardcore")) {
                member.setIsHardcore((Boolean) payload.get("isHardcore"));
            }
            if (payload.containsKey("bomberIntervalMinutes")) {
                member.setBomberIntervalMinutes(Integer.valueOf(payload.get("bomberIntervalMinutes").toString()));
            }
            subscriptionMemberRepository.save(member);
        });

        return ResponseEntity.ok().build();
    }
}