package com.dionialves.AsteraComm.auth;

import java.time.LocalDateTime;

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
