package com.dionialves.AsteraComm.domain.core.user.dto;

import java.time.LocalDateTime;

import com.dionialves.AsteraComm.domain.core.user.User;
import com.dionialves.AsteraComm.domain.core.user.UserRole;

public record UserResponseDTO(
        Long id,
        String name,
        String username,
        UserRole role,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public UserResponseDTO(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
