package com.example.bankcards.controller;

import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.UpdateCardStatusRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@Tag(
        name = "Cards API",
        description = "API для управления картами"
)
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Получить карту по ID", description = "ADMIN или владелец карты может получить данные карты")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardInfoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Ошибка расшифровки карты",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and @cardService.isOwner(#cardId, principal.username)")
    @GetMapping("/{cardId}")
    public ResponseEntity<CardInfoResponse> getCard(
            @Parameter(description = "Идентификатор карты", required = true)
            @PathVariable UUID cardId
    ) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @Operation(summary = "Получить список карт", description = "ADMIN видит все карты, USER может видеть только свои")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница карт получена"),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public Page<CardInfoResponse> getCards(
            @Parameter(description = "Статус карты")
            @RequestParam(required = false) CardStatus status,
            @Parameter(description = "Имя пользователя", example = "john_doe1999")
            @RequestParam(required = false) String username,

            @ParameterObject Pageable pageable
    ) {
        return cardService.getCards(status, username, pageable);
    }

    @Operation(summary = "Обновить статус карты", description = "ADMIN или владелец карты может обновить статус")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус карты обновлён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardInfoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка смены статуса карты",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and @cardService.isOwner(#cardId, principal.username)")
    @PatchMapping("/{cardId}")
    public ResponseEntity<CardInfoResponse> updateCardStatus(
            @Parameter(description = "Идентификатор карты", required = true)
            @PathVariable UUID cardId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Запрос на обновление статуса карты",
                    content = @Content(schema = @Schema(implementation = UpdateCardStatusRequest.class)),
                    required = true
            )
            @RequestBody UpdateCardStatusRequest request
    ) {
        CardInfoResponse card = cardService.updateCardStatus(cardId, request);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Создать карту", description = "Только ADMIN может создать карту")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Карта создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardInfoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Неверные данные владельца",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CardInfoResponse> createCard(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Запрос на создание карты",
                    content = @Content(schema = @Schema(implementation = CreateCardRequest.class)),
                    required = true
            )
            @RequestBody CreateCardRequest request
    ) {
        request.validate();

        CardInfoResponse response = cardService.createCard(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{cardId}")
                .buildAndExpand(response.cardId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Удалить карту", description = "Только ADMIN может удалить карту")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteCard(
            @Parameter(description = "Идентификатор карты", required = true)
            @PathVariable UUID cardId
    ) {
        cardService.deleteCard(cardId);

        return ResponseEntity.noContent().build();
    }
}
