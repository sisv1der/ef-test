package com.example.bankcards.util;

import java.security.SecureRandom;

public class CardGenerator {

    private static final String[] BINS = {"220000", "220001", "220002", "220003", "220004"};
    private static final SecureRandom RANDOM = new SecureRandom();

    private CardGenerator() {}

    public static String generatePan() {
        String bin = BINS[RANDOM.nextInt(BINS.length)];
        StringBuilder pan = new StringBuilder(bin);

        for (int i = 0; i < 9; i++) {
            pan.append(RANDOM.nextInt(10));
        }

        int luhnDigit = computeLuhnDigit(pan.toString());
        pan.append(luhnDigit);

        return pan.toString();
    }

    private static int computeLuhnDigit(String pan) {
        int sum = 0;
        boolean doubleNext = true;
        for (int i = pan.length() - 1; i >= 0; i--) {
            int n = pan.charAt(i) - '0';

            if (doubleNext) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }

            sum += n;
            doubleNext = !doubleNext;
        }
        return (10 - (sum % 10)) % 10;
    }
}
