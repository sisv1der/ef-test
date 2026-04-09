package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление статуса карты")
public record UpdateCardStatusRequest(
        @Schema(
                description = "Новый статус карты",
                example = "ACTIVE",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        CardStatus cardStatus
) {
}
