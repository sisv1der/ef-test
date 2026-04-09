package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Запрос на перевод с одной карты на другую")
public record TransferRequest(
        @Schema(
                description = "Идентификатор карты, с которой будет осуществлён перевод. Не может совпадать с targetCardId",
                example = "64e387b5-f43d-4595-85c8-51cef15e647a",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID sourceCardId,
        @Schema(
                description = "Идентификатор карты, на которую будет осуществлён перевод. Не может совпадать с sourceCardId",
                example = "64e387b5-f43d-4595-85c8-51cef15e647a",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID targetCardId,
        @Schema(
                description = "Количество денег, которое будет переведёно. Должно быть больше нуля",
                example = "100.00",
                minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        BigDecimal amount
) {

    public void validate() {
        if (sourceCardId == null || targetCardId == null) {
            throw new IllegalArgumentException("sourceCardId and targetCardId must not be null");
        }
        if (sourceCardId.equals(targetCardId)) {
            throw new IllegalArgumentException("sourceCardId and targetCardId must not be the same");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }
}
