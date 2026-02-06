package com.dionialves.AsteraComm.auth;

public record UserUpdateDTO(
        String name,
        UserRole role,
        Boolean enabled) {
}
