package com.dionialves.AsteraComm.user.dto;

import com.dionialves.AsteraComm.user.UserRole;

public record RegisterDTO(String name, String username, String password, UserRole role) {
}
