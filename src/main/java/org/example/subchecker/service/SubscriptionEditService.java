package org.example.subchecker.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.subchecker.core.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEditService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void updateName(Long subId, String newName) {
        subscriptionRepository.findById(subId).ifPresent(sub -> {
            sub.setServiceName(newName);
            subscriptionRepository.save(sub);
            log.info("Подписка ID {}: имя изменено на {}", subId, newName);
        });
    }

    @Transactional
    public void updatePrice(Long subId, Double newPrice) {
        subscriptionRepository.findById(subId).ifPresent(sub -> {
            sub.setPrice(newPrice);
            subscriptionRepository.save(sub);
            log.info("Подписка ID {}: цена изменена на {}", subId, newPrice);
        });
    }

    @Transactional
    public void updateUrl(Long subId, String newUrl) {
        subscriptionRepository.findById(subId).ifPresent(sub -> {
            sub.setPaymentUrl(newUrl);
            subscriptionRepository.save(sub);
        });
    }

    @Transactional
    public void updateNotes(Long subId, String newNotes) {
        subscriptionRepository.findById(subId).ifPresent(sub -> {
            sub.setNotes(newNotes);
            subscriptionRepository.save(sub);
        });
    }
}