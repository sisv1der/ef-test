package com.example.bankcards.controller;

import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.UpdateCardStatusRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and @cardService.isOwner(#cardId, principal.username)")
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCard(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public Page<?> getCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String username,
            Pageable pageable
    ) {
        return cardService.getCards(status, username, pageable);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') and @cardService.isOwner(#cardId, principal.username)")
    @PatchMapping("/{cardId}")
    public ResponseEntity<?> updateCardStatus(
            @PathVariable UUID cardId,
            @RequestBody UpdateCardStatusRequest request
    ) {
        CardInfoResponse card = cardService.updateCardStatus(cardId, request);
        return ResponseEntity.ok(card);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CreateCardRequest request) {
        if (request.ownerName().isBlank()) {
            throw new IllegalArgumentException("Owner name must not be blank");
        }

        CardInfoResponse response = cardService.createCard(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{cardId}")
                .buildAndExpand(response.cardId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);

        return ResponseEntity.noContent().build();
    }
}
