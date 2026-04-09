package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ на запрос на вход в учётную запись")
public record LoginResponse(
        @Schema(
                description = "JSON Web Token"
        )
        String token
) {

}