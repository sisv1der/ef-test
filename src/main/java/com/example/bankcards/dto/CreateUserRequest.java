package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на создание пользователя")
public record CreateUserRequest(
        @Schema(
                description = "Username нового пользователя",
                example = "john_doe123",
                minLength = 6,
                maxLength = 32,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String username,
        @Schema(
                description = "Пароль нового пользователя",
                example = "v3ry_s7r0n9_p4$$w0rd",
                minLength = 6,
                maxLength = 32,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String password,
        @Schema(
                description = "Роль нового пользователя",
                example = "ADMIN",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Role role
) {

    public void validateUsername() {
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
    }

    public void validatePassword() {
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
    }

    public void validateRole() {
        if (role == null) {
            throw new IllegalArgumentException("Role must be provided");
        }
    }

    public void validate() {
        validateUsername(); validatePassword(); validateRole();
    }
}
