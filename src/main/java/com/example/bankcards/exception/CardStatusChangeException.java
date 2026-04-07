package com.example.bankcards.exception;

public class CardStatusChangeException extends RuntimeException {
    public CardStatusChangeException(String message) {
        super(message);
    }
}
