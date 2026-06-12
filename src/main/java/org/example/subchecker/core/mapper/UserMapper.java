package org.example.subchecker.core.mapper;


import org.example.subchecker.core.dto.UserDTO;
import org.example.subchecker.core.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toDTO(User user){
        if(user == null) return null;
        return UserDTO.builder()
                .telegramId(user.getTelegramId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .role(user.getRole())
                .preferredNotificationTime(user.getPreferredNotificationTime())
                .build();

    }
}
