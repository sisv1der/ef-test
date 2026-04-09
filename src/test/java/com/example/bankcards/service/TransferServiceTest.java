package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardInactiveException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private TransferService transferService;

    private TransferRequest transferRequest;
    private User owner;
    private Card sourceCard;
    private Card targetCard;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .username("username")
                .passwordHash("password")
                .build();

        sourceCard = Card.builder()
                .owner(owner)
                .ownerName("username")
                .iv("iv")
                .encryptedNumber("pan")
                .salt("salt")
                .balance(BigDecimal.valueOf(10000))
                .build();

        targetCard = Card.builder()
                .owner(owner)
                .ownerName("username")
                .iv("iv")
                .encryptedNumber("pan")
                .salt("salt")
                .balance(BigDecimal.valueOf(10000))
                .build();

        transferRequest = new TransferRequest(sourceCard.getId(), targetCard.getId(), BigDecimal.valueOf(2000));
    }

    @Test
    void transfer_shouldTransfer_whenIsUserAndIsOwnerAndCardsAreActiveAndFundsAreSufficient() {
        when(cardRepository.findByIdForUpdate(eq(sourceCard.getId())))
                .thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdForUpdate(eq(targetCard.getId())))
                .thenReturn(Optional.of(targetCard));
        when(authenticationService.getUsername())
                .thenReturn(owner.getUsername());

        BigDecimal initialSourceBalance = BigDecimal.valueOf(sourceCard.getBalance().doubleValue());
        BigDecimal initialTargetBalance = BigDecimal.valueOf(targetCard.getBalance().doubleValue());
        BigDecimal toAdd = transferRequest.amount();
        BigDecimal expectedSourceBalance = initialSourceBalance.subtract(toAdd);
        BigDecimal expectedTargetBalance = initialTargetBalance.add(toAdd);

        transferService.transfer(transferRequest);

        assertEquals(expectedSourceBalance.longValue(), sourceCard.getBalance().longValue());
        assertEquals(expectedTargetBalance.longValue(), targetCard.getBalance().longValue());

        verify(cardRepository, times(2)).findByIdForUpdate(any(UUID.class));
    }

    @Test
    void transfer_shouldThrowInsufficientFunds_whenBalanceTooLow() {
        when(cardRepository.findByIdForUpdate(eq(sourceCard.getId())))
                .thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdForUpdate(eq(targetCard.getId())))
                .thenReturn(Optional.of(targetCard));
        when(authenticationService.getUsername())
                .thenReturn(owner.getUsername());

        transferRequest = new TransferRequest(
                sourceCard.getId(),
                targetCard.getId(),
                sourceCard.getBalance().add(BigDecimal.valueOf(1))
        );

        assertThrows(InsufficientFundsException.class, () -> transferService.transfer(transferRequest));
    }

    @Test
    void transfer_shouldThrowCardInactive_whenSourceInactive() {
        sourceCard.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findByIdForUpdate(eq(sourceCard.getId())))
                .thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdForUpdate(eq(targetCard.getId())))
                .thenReturn(Optional.of(targetCard));
        when(authenticationService.getUsername())
                .thenReturn(owner.getUsername());

        assertThrows(CardInactiveException.class, () -> transferService.transfer(transferRequest));
    }

    @Test
    void transfer_shouldThrowAccessDenied_whenCardsNotOwned() {
        User otherUser = User.builder().username("other").passwordHash("password").build();
        sourceCard.setOwner(otherUser);

        when(cardRepository.findByIdForUpdate(eq(sourceCard.getId())))
                .thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdForUpdate(eq(targetCard.getId())))
                .thenReturn(Optional.of(targetCard));
        when(authenticationService.getUsername())
                .thenReturn(owner.getUsername());

        assertThrows(AccessDeniedException.class, () -> transferService.transfer(transferRequest));
    }

    @Test
    void transfer_shouldThrowEntityNotFound_whenCardIsAbsent() {
        when(cardRepository.findByIdForUpdate(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> transferService.transfer(transferRequest));
    }

    @Test
    void transfer_shouldCallFindByIdInConsistentOrder() {
        UUID firstId = transferRequest.sourceCardId().compareTo(transferRequest.targetCardId()) < 0
                ? transferRequest.sourceCardId() : transferRequest.targetCardId();
        UUID secondId = firstId.equals(transferRequest.sourceCardId())
                ? transferRequest.targetCardId() : transferRequest.sourceCardId();

        when(cardRepository.findByIdForUpdate(eq(firstId))).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdForUpdate(eq(secondId))).thenReturn(Optional.of(targetCard));
        when(authenticationService.getUsername()).thenReturn(owner.getUsername());

        transferService.transfer(transferRequest);

        verify(cardRepository).findByIdForUpdate(firstId);
        verify(cardRepository).findByIdForUpdate(secondId);
    }
}