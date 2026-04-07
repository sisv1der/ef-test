package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record CardInfoResponse(
        UUID cardId,
        String maskedNumber,
        CardStatus cardStatus,
        String ownerName,
        UUID ownerId,
        YearMonth expirationDate,
        BigDecimal balance
) {
}
