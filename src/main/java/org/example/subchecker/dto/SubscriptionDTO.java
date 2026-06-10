package org.example.subchecker.dto;

import lombok.*;
import org.example.subchecker.model.Currency;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private Long id;
    private String serviceName;
    private String category;
    private Double price;
    private Currency currency;
    private LocalDate nextPaymentDate;
    private Integer periodDays;
    private String paymentUrl;
    private String notes;
    private Boolean isAcknowledged;
    private Long ownerId;
    private boolean isFamily;
    private String inviteCode;
    private Long daysLeft;

    private Boolean isHardcore;
    private Integer bomberIntervalMinutes;
}