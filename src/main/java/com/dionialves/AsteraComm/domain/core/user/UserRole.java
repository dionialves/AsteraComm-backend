package com.dionialves.AsteraComm.domain.core.user;

public enum UserRole {
    SUPER_ADMIN("super_admin"),
    ADMIN("admin"),
    USER("user");

    private String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
