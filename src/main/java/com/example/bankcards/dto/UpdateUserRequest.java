package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление пользователяё")
public record UpdateUserRequest(
        @Schema(
                description = "Новый username пользователя",
                example = "john_doe321",
                minLength = 6,
                maxLength = 32,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String username,
        @Schema(
                description = "Новый пароль пользователя",
                example = "not_very_strong_password",
                minLength = 6,
                maxLength = 32,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String password,
        @Schema(
                description = "Новая роль пользователя",
                example = "ADMIN",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Role role,
        @Schema(
                description = "Статус пользователя",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Boolean isActive
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

    public void validateIsActive() {
        if (isActive == null) {
            throw new IllegalArgumentException("Active must be provided");
        }
    }

    public void validateRole() {
        if (role == null) {
            throw new IllegalArgumentException("Role must be provided");
        }
    }

    public void validate() {
        validateUsername(); validatePassword(); validateRole(); validateIsActive();
    }
}
