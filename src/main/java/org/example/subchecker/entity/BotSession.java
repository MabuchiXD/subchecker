package org.example.subchecker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.subchecker.model.Currency;
import java.time.LocalTime;

@Entity
@Table(name = "bot_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BotSession {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Builder.Default
    private String state = "IDLE";

    @Column(columnDefinition = "TEXT")
    private String tempName;
    private Double tempPrice;
    private String tempUrl;
    private String tempCategory;
    private Integer tempPeriodDays;
    private Long tempSubId;
    private String tempNotes;

    @Enumerated(EnumType.STRING)
    private Currency tempCurrency;

    @Column(name = "temp_pref_time")
    private LocalTime tempPrefTime;

    @Column(name = "temp_timezone_offset")
    private Integer tempTimezoneOffset;

    @Enumerated(EnumType.STRING)
    @Column(name = "temp_default_currency")
    private Currency tempDefaultCurrency;
}