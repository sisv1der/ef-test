package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardInactiveException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {

    private final CardRepository cardRepository;
    private final AuthenticationService authenticationService;

    public TransferService(CardRepository cardRepository, AuthenticationService authenticationService) {
        this.cardRepository = cardRepository;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public void transfer(TransferRequest transferRequest) {
        UUID firstId = transferRequest.sourceCardId().compareTo(transferRequest.targetCardId()) < 0
                ? transferRequest.sourceCardId()
                : transferRequest.targetCardId();
        UUID secondId = firstId.equals(transferRequest.sourceCardId())
                ? transferRequest.targetCardId()
                : transferRequest.sourceCardId();

        Card first = getCard(firstId);
        Card second = getCard(secondId);

        Card source = firstId.equals(transferRequest.sourceCardId()) ? first : second;
        Card target = source == first ? second : first;

        if (!isOwner(source, target)) {
            throw new AccessDeniedException("Cards do not belong to this user");
        }
        if (isNotActive(source) || isNotActive(target)) {
            throw new CardInactiveException("Cards are not active");
        }
        if (!isEnoughMoney(source, transferRequest.amount())) {
            throw new InsufficientFundsException("Not enough money");
        }

        transfer(source, target, transferRequest.amount());
    }

    private void transfer(Card source, Card target, BigDecimal amount) {
        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));
    }

    private boolean isNotActive(Card card) {
        return card.getStatus() != CardStatus.ACTIVE;
    }

    private boolean isEnoughMoney(Card source, BigDecimal amount) {
        return source.getBalance().compareTo(amount) >= 0;
    }

    private boolean isOwner(Card source, Card target) {
        String username = authenticationService.getUsername();

        return source.getOwner().getUsername().equals(username) && target.getOwner().getUsername().equals(username);
    }

    private Card getCard(UUID cardId) {
        return cardRepository.findByIdForUpdate(cardId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
