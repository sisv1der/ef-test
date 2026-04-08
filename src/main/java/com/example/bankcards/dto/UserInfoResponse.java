package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record UserInfoResponse(
        UUID id,
        String username,
        Role role,
        boolean isActive,
        Instant createdAt
) {
}
