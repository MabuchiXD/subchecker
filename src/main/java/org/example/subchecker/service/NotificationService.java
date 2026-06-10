package org.example.subchecker.service;

import org.example.subchecker.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class NotificationService {

    public boolean isItTimeToNotify(User user) {

        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);

        LocalTime userLocalTime = nowUtc.plusHours(user.getTimezoneOffset())
                .toLocalTime()
                .withSecond(0)
                .withNano(0);

        LocalTime prefTime = user.getPreferredNotificationTime();

        return userLocalTime.equals(prefTime) || userLocalTime.equals(prefTime.plusHours(8));
    }
}