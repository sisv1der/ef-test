package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/transfers")
@Tag(
        name = "Transfers API",
        description = "API для финансовых переводов"
)
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(
            summary = "Перевести деньги между своими картами",
            description = "Только владелец обеих карт может переводить деньги между этими картами"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод произведён успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации тела запроса, либо на карте недостаточно денег",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Ошибка доступа: либо не совпадает роль, либо одна из карт не принадлежит пользователю, либо одна из карт неактивна",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Запрос на финансовый перевод",
                    content = @Content(schema = @Schema(implementation = TransferRequest.class)),
                    required = true
            )
            @RequestBody TransferRequest transferRequest
    ) {
        transferRequest.validate();

        transferService.transfer(transferRequest);

        return ResponseEntity.ok().build();
    }
}
