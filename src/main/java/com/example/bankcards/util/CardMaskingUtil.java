package com.example.bankcards.util;

public class CardMaskingUtil {

    private CardMaskingUtil() {}

    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("Invalid card number");
        }

        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
