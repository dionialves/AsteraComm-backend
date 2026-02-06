package com.dionialves.AsteraComm.auth;

public record UserCreateDTO(
        String name,
        String username,
        String password,
        UserRole role) {
}
