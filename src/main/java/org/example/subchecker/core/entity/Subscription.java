package org.example.subchecker.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.subchecker.core.model.Currency;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subs_next_payment", columnList = "next_payment_date"),
        @Index(name = "idx_subs_owner_id", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название сервиса не может быть пустым")
    @Column(nullable = false, length = 100)
    private String serviceName;

    private String category;

    @Positive(message = "Цена не может быть отрицательной")
    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Currency currency = Currency.RUB;

    @NotNull(message = "Дата платежа обязательна")
    private LocalDate lastPaymentDate;

    @NotNull(message = "Дата следующего платежа обязательна")
    private LocalDate nextPaymentDate;

    private Integer periodDays;

    @Column(name = "payment_url", length = 512)
    private String paymentUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isAcknowledged = false;

    @Column(name = "invite_code", unique = true)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SubscriptionMember> members;

    public void renew(int days) {
        this.lastPaymentDate = LocalDate.now();
        this.nextPaymentDate = this.nextPaymentDate.plusDays(days);
        this.isAcknowledged = false;
    }
}