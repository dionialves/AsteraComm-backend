package com.dionialves.AsteraComm.domain.auth.dto;

import com.dionialves.AsteraComm.domain.auth.entity.UserRole;

public record RegisterDTO(String username, String password, UserRole role) {
}
