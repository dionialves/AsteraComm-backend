package com.dionialves.AsteraComm.auth;

public record RegisterDTO(String name, String username, String password, UserRole role) {
}
