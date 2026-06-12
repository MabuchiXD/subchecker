package org.example.subchecker.core.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "subscription_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    // ЛИЧНЫЕ НАСТРОЙКИ
    @Builder.Default
    @Column(name = "is_hardcore")
    private Boolean isHardcore = false;

    @Builder.Default
    @Column(name = "bomber_interval_minutes")
    private Integer bomberIntervalMinutes = 60;

    @Column(name = "last_bomber_notify_sent")
    private LocalDateTime lastBomberNotifySent;

    @Column(name = "last_daily_notify_at")
    private java.time.LocalDateTime lastDailyNotifyAt;
}