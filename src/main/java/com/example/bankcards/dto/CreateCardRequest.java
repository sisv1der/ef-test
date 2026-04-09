package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Запрос на создание карты")
public record CreateCardRequest(
        @Schema(
                description = "Идентификатор пользователя",
                example = "64e387b5-f43d-4595-85c8-51cef15e647a",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID userId,
        @Schema(
                description = "Имя владельца",
                example = "IVAN IVANOV",
                minLength = 5,
                maxLength = 32,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String ownerName
) {

    public void validate() {
        if (ownerName == null) {
            throw new IllegalArgumentException("Owner name must be provided");
        }
        if (ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name must not be blank");
        }
        if (ownerName.length() > 32) {
            throw new IllegalArgumentException("Owner name must not be longer than 32 characters");
        }
        if (ownerName.length() < 5) {
            throw new IllegalArgumentException("Owner name must have at least 5 characters");
        }
    }
}
