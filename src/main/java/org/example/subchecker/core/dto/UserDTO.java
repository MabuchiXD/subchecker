package org.example.subchecker.core.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class UserDTO {
    private Long telegramId;
    private String username;
    private String firstName;
    private String role;

    private LocalTime preferredNotificationTime;
    private Boolean hardCoreMode;
}
