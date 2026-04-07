package com.example.bankcards.util;

import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.entity.CardStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class CardMapper {

    private final CardMaskingUtil cardMaskingUtil;

    public CardMapper(CardMaskingUtil cardMaskingUtil) {
        this.cardMaskingUtil = cardMaskingUtil;
    }

    public CardInfoResponse map(
            UUID cardId,
            String cardNumber,
            CardStatus cardStatus,
            UUID ownerId,
            String ownerName,
            YearMonth expirationDate,
            BigDecimal balance
    ) {
        String maskedCardNumber = cardMaskingUtil.mask(cardNumber);
        return new CardInfoResponse(cardId, maskedCardNumber, cardStatus, ownerName, ownerId, expirationDate, balance);
    }
}
