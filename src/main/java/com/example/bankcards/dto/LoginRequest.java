package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на вход в учётную запись")
public record LoginRequest(
        @Schema(
                description = "Username пользователя",
                example = "john_doe123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String username,
        @Schema(
                description = "Пароль пользователя",
                example = "v3ry_s7r0n9_p4$$w0rd",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String password
) {

    public void validate() {
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
    }

}
