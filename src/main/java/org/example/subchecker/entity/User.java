package org.example.subchecker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.example.subchecker.model.Currency;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @Column(name = "id")
    private Long telegramId;

    @NotBlank(message = "Username не может быть пустым")
    private String username;

    private String firstName;

    @Column(name = "preferred_notification_time")
    private LocalTime preferredNotificationTime = LocalTime.of(10, 0);


    @Builder.Default
    @Column(name = "timezone_offset")
    private Integer timezoneOffset = 3;

    @Builder.Default
    private String role = "USER";

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Subscription> ownedSubscriptions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SubscriptionMember> memberships;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Currency defaultCurrency = Currency.RUB;
}