package com.example.bankcards.util;

import org.springframework.stereotype.Service;

@Service
public class CardMaskingUtil {

    public String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("Invalid card number");
        }

        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
