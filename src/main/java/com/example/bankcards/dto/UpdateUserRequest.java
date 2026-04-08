package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;

public record UpdateUserRequest(String username, String password, Role role, Boolean isActive) {

    public boolean validateUsername() {
        if (username == null) {
            throw new IllegalArgumentException("Username must be provided");
        }
        if (username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (username.length() < 6) {
            throw new IllegalArgumentException("Username length must be at least 6 characters");
        }
        if (username.length() > 32) {
            throw new IllegalArgumentException("Username length must be at most 32 characters");
        }
        return true;
    }

    public boolean validatePassword() {
        if (password == null) {
            throw new IllegalArgumentException("Password must be provided");
        }
        if (password.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password length must be at least 6 characters");
        }
        if (password.length() > 32) {
            throw new IllegalArgumentException("Password length must be at most 32 characters");
        }
        return true;
    }

    public boolean validateIsActive() {
        if (isActive == null) {
            throw new IllegalArgumentException("Active must be provided");
        }
        return true;
    }

    public boolean validateRole() {
        if (role == null) {
            throw new IllegalArgumentException("Role must be provided");
        }
        return true;
    }

    public void validate() {
        validateUsername(); validatePassword(); validateRole(); validateIsActive();
    }
}
