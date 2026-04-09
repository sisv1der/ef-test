package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Schema(description = "Информация о карте")
public record CardInfoResponse(
        @Schema(
                description = "Идентификатор карты",
                example = "64e387b5-f43d-4595-85c8-51cef15e647a"
        )
        UUID cardId,
        @Schema(
                description = "Замаскированный PAN",
                example = "**** **** **** 1234"
        )
        String maskedNumber,
        @Schema(
                description = "Статус карты",
                example = "ACTIVE"
        )
        CardStatus cardStatus,
        @Schema(
                description = "Имя владельца",
                example = "IVAN IVANOV"
        )
        String ownerName,
        @Schema(
                description = "Идентификатор владельца",
                example = "64e387b5-f43d-4595-85c8-51cef15e647a"
        )
        UUID ownerId,
        @Schema(
                description = "ГОД-МЕСЯЦ окончания срока действия карты",
                example = "2026-04"
        )
        YearMonth expirationDate,
        @Schema(
                description = "Баланс карты",
                example = "100.00"
        )
        BigDecimal balance
) {
}
