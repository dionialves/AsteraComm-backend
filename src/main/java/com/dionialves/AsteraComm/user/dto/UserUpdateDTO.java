package com.dionialves.AsteraComm.user.dto;

import com.dionialves.AsteraComm.user.UserRole;

public record UserUpdateDTO(
        String name,
        UserRole role,
        Boolean enabled) {
}
