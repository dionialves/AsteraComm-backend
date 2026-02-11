package com.dionialves.AsteraComm.domain.core.user.dto;

import com.dionialves.AsteraComm.domain.core.user.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(
        @NotBlank @Size(min = 5, max = 100) String name,
        @NotBlank @Size(min = 5, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull UserRole role) {
}
