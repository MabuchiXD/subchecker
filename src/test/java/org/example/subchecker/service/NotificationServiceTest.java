package org.example.subchecker.service;

import org.example.subchecker.core.entity.User;
import org.example.subchecker.telegram.scheduler.NotificationService;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService();

    @Test
    void testIsItTimeToNotify_WhenTimeMatchesExactly_ShouldReturnTrue() {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        LocalTime currentLocalTime = nowUtc.toLocalTime().withSecond(0).withNano(0);

        User user = User.builder()
                .timezoneOffset(0)
                .preferredNotificationTime(currentLocalTime)
                .username("test_user")
                .build();

        assertTrue(notificationService.isItTimeToNotify(user));
    }

    @Test
    void testIsItTimeToNotify_WhenTimeMatchesPlusEightHours_ShouldReturnTrue() {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        LocalTime currentLocalTime = nowUtc.toLocalTime().withSecond(0).withNano(0);

        // Юзер у которого осталось ещё 8 часов
        LocalTime prefTime = currentLocalTime.minusHours(8);

        User user = User.builder()
                .timezoneOffset(0)
                .preferredNotificationTime(prefTime)
                .username("test_user")
                .build();

        assertTrue(notificationService.isItTimeToNotify(user));
    }

    @Test
    void testIsItTimeToNotify_WhenTimeDoesNotMatch_ShouldReturnFalse() {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        LocalTime currentLocalTime = nowUtc.toLocalTime().withSecond(0).withNano(0);

        // Юзер у которого предпочитаемое время не совпадает
        LocalTime prefTime = currentLocalTime.plusHours(3);

        User user = User.builder()
                .timezoneOffset(0)
                .preferredNotificationTime(prefTime)
                .username("test_user")
                .build();

        assertFalse(notificationService.isItTimeToNotify(user));
    }
}