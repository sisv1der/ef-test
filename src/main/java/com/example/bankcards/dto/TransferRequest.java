package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(UUID sourceCardId, UUID targetCardId, BigDecimal amount) {

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
