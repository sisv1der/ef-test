package com.example.bankcards.dto;

import java.util.UUID;

public record CreateCardRequest(UUID userId, String ownerName) {
}
