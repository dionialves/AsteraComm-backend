package com.dionialves.AsteraComm.domain.core.user.dto;

import com.dionialves.AsteraComm.domain.core.user.UserRole;

import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @Size(min = 5, max = 100) String name,
        UserRole role,
        Boolean enabled) {
}
