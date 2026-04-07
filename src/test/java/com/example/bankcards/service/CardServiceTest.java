package com.example.bankcards.service;

import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.EncryptedRecord;
import com.example.bankcards.dto.UpdateCardStatusRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardStatusChangeException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private CardNumberEncryptionService cardNumberEncryptionService;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;
    private CardInfoResponse cardInfoResponse;
    private UpdateCardStatusRequest updateCardStatusRequest;
    private CardInfoResponse updateCardStatusResponse;
    private CreateCardRequest createCardRequest;
    private EncryptedRecord encryptedRecord;
    private PageRequest pageable;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("username");

        card = Card.builder()
                .id(UUID.randomUUID())
                .salt("salt")
                .iv("iv")
                .encryptedNumber("cardNumber")
                .owner(user)
                .ownerName("ownerName")
                .build();

        cardInfoResponse = new CardInfoResponse(
                card.getId(),
                "decryptedCardNumber",
                card.getStatus(),
                card.getOwnerName(),
                card.getOwner().getId(),
                card.getExpirationDate(),
                card.getBalance()
        );

        updateCardStatusRequest = new UpdateCardStatusRequest(CardStatus.BLOCKED);

        updateCardStatusResponse = new CardInfoResponse(
                card.getId(),
                "decryptedCardNumber",
                CardStatus.BLOCKED,
                card.getOwnerName(),
                card.getOwner().getId(),
                card.getExpirationDate(),
                card.getBalance()
        );

        createCardRequest = new CreateCardRequest(user.getId(), card.getOwnerName());

        encryptedRecord = new EncryptedRecord(card.getSalt(), card.getIv(), card.getEncryptedNumber());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void isOwner_shouldReturnTrue_whenUserIsOwner() {
        when(cardRepository.existsByIdAndOwnerUsername(card.getId(), user.getUsername()))
                .thenReturn(true);

        boolean result = cardService.isOwner(card.getId(), user.getUsername());

        assertTrue(result);

        verify(cardRepository).existsByIdAndOwnerUsername(card.getId(), user.getUsername());
    }

    @Test
    void isOwner_shouldReturnFalse_whenUserIsNotOwner() {
        when(cardRepository.existsByIdAndOwnerUsername(card.getId(), user.getUsername()))
                .thenReturn(false);

        boolean result = cardService.isOwner(card.getId(), user.getUsername());

        assertFalse(result);

        verify(cardRepository).existsByIdAndOwnerUsername(card.getId(), user.getUsername());
    }

    @Test
    void getCard_shouldReturnCard_whenCardExists() {
        when(cardRepository.findById(card.getId()))
                .thenReturn(Optional.of(card));
        when(cardNumberEncryptionService.decryptCardNumber(card))
                .thenReturn(cardInfoResponse.maskedNumber());
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any())).thenReturn(cardInfoResponse);

        CardInfoResponse result = cardService.getCard(card.getId());

        assertNotNull(result);
        assertEquals(cardInfoResponse, result);

        verify(cardRepository).findById(card.getId());
        verify(cardNumberEncryptionService).decryptCardNumber(card);
    }

    @Test
    void getCard_shouldThrow_whenCardDoesNotExist() {
        when(cardRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.getCard(UUID.randomUUID()));

        verify(cardRepository).findById(any());
    }

    @Test
    void getCards_whenIsAdminAndStatusProvidedAndUsernameProvided() {
        when(authenticationService.isAdmin(any()))
                .thenReturn(true);
        when(cardRepository.findByOwnerUsernameAndStatus(any(Pageable.class), any(), any()))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cardInfoResponse);

        Page<CardInfoResponse> result = cardService.getCards(pageable, CardStatus.ACTIVE, "username");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(cardInfoResponse, result.getContent().get(0));

        verify(authenticationService).isAdmin(any());
        verify(cardRepository).findByOwnerUsernameAndStatus(any(), any(), any());
        verify(cardRepository, never()).findByStatus(any(), any());
        verify(cardRepository, never()).findByOwnerUsername(any(), any());
        verify(cardRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getCards_whenIsAdminAndStatusNotProvidedAndUsernameProvided() {
        when(authenticationService.isAdmin(any()))
                .thenReturn(true);
        when(cardRepository.findByOwnerUsername(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cardInfoResponse);

        Page<CardInfoResponse> result = cardService.getCards(pageable, null, "username");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(cardInfoResponse, result.getContent().get(0));

        verify(authenticationService).isAdmin(any());
        verify(cardRepository, never()).findByOwnerUsernameAndStatus(any(), any(), any());
        verify(cardRepository, never()).findByStatus(any(), any());
        verify(cardRepository).findByOwnerUsername(any(), any());
        verify(cardRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getCards_whenIsAdminAndStatusProvidedAndUsernameNotProvided() {
        when(authenticationService.isAdmin(any()))
                .thenReturn(true);
        when(cardRepository.findByStatus(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cardInfoResponse);

        Page<CardInfoResponse> result = cardService.getCards(pageable, CardStatus.ACTIVE, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(cardInfoResponse, result.getContent().get(0));

        verify(authenticationService).isAdmin(any());
        verify(cardRepository, never()).findByOwnerUsernameAndStatus(any(), any(), any());
        verify(cardRepository).findByStatus(any(), any());
        verify(cardRepository, never()).findByOwnerUsername(any(), any());
        verify(cardRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getCards_whenIsAdminAndStatusNotProvidedAndUsernameNotProvided() {
        when(authenticationService.isAdmin(any()))
                .thenReturn(true);
        when(cardRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cardInfoResponse);

        Page<CardInfoResponse> result = cardService.getCards(pageable, null, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(cardInfoResponse, result.getContent().get(0));

        verify(authenticationService).isAdmin(any());
        verify(cardRepository, never()).findByOwnerUsernameAndStatus(any(), any(), any());
        verify(cardRepository, never()).findByStatus(any(), any());
        verify(cardRepository, never()).findByOwnerUsername(any(), any());
        verify(cardRepository).findAll(any(Pageable.class));
    }

    @Test
    void getCards_whenIsNotAdminAndStatusProvided() {
        when(authenticationService.isAdmin(any()))
                .thenReturn(false);
        when(cardRepository.findByOwnerUsernameAndStatus(any(Pageable.class), any(), any()))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cardInfoResponse);

        Page<CardInfoResponse> result = cardService.getCards(pageable, CardStatus.ACTIVE, "username");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(cardInfoResponse, result.getContent().get(0));

        verify(authenticationService).isAdmin(any());
        verify(cardRepository).findByOwnerUsernameAndStatus(any(), any(), any());
        verify(cardRepository, never()).findByStatus(any(), any());
        verify(cardRepository, never()).findByOwnerUsername(any(), any());
        verify(cardRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getCards_whenIsNotAdminAndStatusNotProvided() {
        when(authenticationService.isAdmin(any()))
                .thenReturn(false);
        when(cardRepository.findByOwnerUsername(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cardInfoResponse);

        Page<CardInfoResponse> result = cardService.getCards(pageable, null, "username");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(cardInfoResponse, result.getContent().get(0));

        verify(authenticationService).isAdmin(any());
        verify(cardRepository, never()).findByOwnerUsernameAndStatus(any(), any(), any());
        verify(cardRepository, never()).findByStatus(any(), any());
        verify(cardRepository).findByOwnerUsername(any(), any());
        verify(cardRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void updateCardStatus_shouldUpdate_whenIsAdmin() {
        when(cardRepository.findById(card.getId()))
                .thenReturn(Optional.of(card));
        when(authenticationService.isAdmin(any()))
                .thenReturn(true);
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(updateCardStatusResponse);

        CardInfoResponse result = cardService.updateCardStatus(card.getId(), updateCardStatusRequest);

        assertNotNull(result);
        assertEquals(updateCardStatusResponse, result);
        assertEquals(CardStatus.BLOCKED, card.getStatus());

        verify(authenticationService).isAdmin(any());
        verify(cardRepository).findById(any());
    }

    @Test
    void updateCardStatus_shouldUpdate_whenIsNotAdminAndProvidedStatusIsBlocked() {
        when(cardRepository.findById(card.getId()))
                .thenReturn(Optional.of(card));
        when(authenticationService.isAdmin(any()))
                .thenReturn(false);
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(updateCardStatusResponse);

        CardInfoResponse result = cardService.updateCardStatus(card.getId(), updateCardStatusRequest);

        assertNotNull(result);
        assertEquals(updateCardStatusResponse, result);
        assertEquals(CardStatus.BLOCKED, card.getStatus());

        verify(authenticationService).isAdmin(any());
        verify(cardRepository).findById(any());
    }

    @Test
    void updateCardStatus_shouldThrow_whenIsNotAdminAndAlreadyBlocked() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(card.getId()))
                .thenReturn(Optional.of(card));
        when(authenticationService.isAdmin(any()))
                .thenReturn(false);

        assertThrows(CardStatusChangeException.class, () -> cardService.updateCardStatus(card.getId(), updateCardStatusRequest));
        assertEquals(CardStatus.BLOCKED, card.getStatus());

        verify(authenticationService).isAdmin(any());
        verify(cardRepository).findById(any());
    }

    @Test
    void updateCardStatus_shouldThrow_whenIsNotAdminAndProvidedStatusIsNotBlocked() {
        card.setStatus(CardStatus.BLOCKED);
        updateCardStatusRequest = new UpdateCardStatusRequest(CardStatus.ACTIVE);
        when(cardRepository.findById(card.getId()))
                .thenReturn(Optional.of(card));
        when(authenticationService.isAdmin(any()))
                .thenReturn(false);

        assertThrows(CardStatusChangeException.class, () -> cardService.updateCardStatus(card.getId(), updateCardStatusRequest));
        assertEquals(CardStatus.BLOCKED, card.getStatus());

        verify(authenticationService).isAdmin(any());
        verify(cardRepository).findById(any());
    }

    @Test
    void createCard_shouldCreate_whenUserExists() {
        when(userService.findById(any()))
                .thenReturn(Optional.of(user));
        when(cardNumberEncryptionService.encryptCardNumber(any()))
                .thenReturn(encryptedRecord);
        when(cardMapper.map(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cardInfoResponse);

        CardInfoResponse result = cardService.createCard(createCardRequest);

        assertNotNull(result);
        assertEquals(cardInfoResponse, result);

        verify(cardRepository).save(any());
    }

    @Test
    void createCard_shouldThrow_whenUserDoesNotExist() {
        when(userService.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.createCard(createCardRequest));

        verify(cardRepository, never()).save(any());
    }

    @Test
    void deleteCard_shouldDelete_whenCardExists() {
        when(cardRepository.findById(card.getId()))
                .thenReturn(Optional.of(card));

        cardService.deleteCard(card.getId());

        verify(cardRepository).delete(any());
    }

    @Test
    void deleteCard_shouldThrow_whenCardDoesNotExist() {
        when(cardRepository.findById(card.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.deleteCard(card.getId()));

        verify(cardRepository, never()).delete(any());
    }
}