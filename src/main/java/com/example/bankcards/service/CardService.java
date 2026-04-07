package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardStatusChangeException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardGenerator;
import com.example.bankcards.util.CardMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardNumberEncryptionService cardNumberEncryptionService;
    private final UserService userService;
    private final CardMapper cardMapper;
    private final AuthenticationService authenticationService;

    public CardService(
            CardRepository cardRepository,
            CardNumberEncryptionService cardNumberEncryptionService,
            UserService userService, CardMapper cardMapper,
            AuthenticationService authenticationService) {
        this.cardRepository = cardRepository;
        this.cardNumberEncryptionService = cardNumberEncryptionService;
        this.userService = userService;
        this.cardMapper = cardMapper;
        this.authenticationService = authenticationService;
    }

    public boolean isOwner(UUID cardId, String username) {
        return cardRepository.existsByIdAndOwnerUsername(cardId, username);
    }

    public CardInfoResponse getCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card with ID: " + cardId + " not found"));

        return getCardInfo(card);
    }

    public Page<CardInfoResponse> getCards(Pageable pageable, CardStatus status, String username) {
        Page<Card> cards;
        if (isAdmin()) {
            if (username != null && status != null) {
                cards = cardRepository.findByOwnerUsernameAndStatus(pageable, username, status);
            } else if (status != null) {
                cards = cardRepository.findByStatus(pageable, status);
            } else if (username != null) {
                cards = cardRepository.findByOwnerUsername(pageable, username);
            } else {
                cards = cardRepository.findAll(pageable);
            }
        } else {
            username = getUsername();
            if (status != null) {
                cards = cardRepository.findByOwnerUsernameAndStatus(pageable, username, status);
            } else {
                cards = cardRepository.findByOwnerUsername(pageable, username);
            }
        }

        return cards.map(this::getCardInfo);
    }

    private CardInfoResponse getCardInfo(Card card) {
        String decryptedCardNumber = cardNumberEncryptionService.decryptCardNumber(card);

        return cardMapper.map(
                card.getId(),
                decryptedCardNumber,
                card.getStatus(),
                card.getOwner().getId(),
                card.getOwnerName(),
                card.getExpirationDate(),
                card.getBalance()
        );
    }

    @Transactional
    public CardInfoResponse updateCardStatus(UUID cardId, UpdateCardStatusRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card with ID: " + cardId + " not found"));

        if (!isAdmin()) {
            if (card.getStatus() == CardStatus.BLOCKED) {
                throw new CardStatusChangeException("You can't change status of blocked cards");
            }
            if (request.cardStatus() != CardStatus.BLOCKED) {
                throw new CardStatusChangeException("Non-admins can only block cards");
            }
        }

        card.setStatus(request.cardStatus());

        return getCardInfo(card);
    }

    private boolean isAdmin() {
        return authenticationService.isAdmin(authenticationService.getAuthentication());
    }

    private String getUsername() {
        return authenticationService.getUsername();
    }

    @Transactional
    public CardInfoResponse createCard(CreateCardRequest request) {
        User user = userService.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + request.userId() + " not found"));

        String pan = CardGenerator.generatePan();
        EncryptedRecord encryptedPan = cardNumberEncryptionService.encryptCardNumber(pan);
        Card card = Card.builder()
                .salt(encryptedPan.salt())
                .iv(encryptedPan.iv())
                .encryptedNumber(encryptedPan.cipherText())
                .owner(user)
                .ownerName(request.ownerName())
                .id(UUID.randomUUID())
                .build();

        cardRepository.save(card);
        return getCardInfo(card);
    }

    public void deleteCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                        .orElseThrow(() -> new EntityNotFoundException("Card with ID: " + cardId + " not found"));

        cardRepository.delete(card);
    }
}
