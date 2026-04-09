package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Информация о пользователе")
public record UserInfoResponse(
        @Schema(
                description = "Идентификатор пользователя",
                example = "64e387b5-f43d-4595-85c8-51cef15e647a"
        )
        UUID id,
        @Schema(
                description = "Username пользователя",
                example = "john_doe123"
        )
        String username,
        @Schema(
                description = "Роль пользователя",
                example = "ADMIN"
        )
        Role role,
        @Schema(
                description = "Статус пользователя (активен или нет)",
                example = "true"
        )
        boolean isActive,
        @Schema(
                description = "Timestamp момента создания пользователя",
                example = "2026-04-09T08:15:30.123456Z"
        )
        Instant createdAt
) {
}
