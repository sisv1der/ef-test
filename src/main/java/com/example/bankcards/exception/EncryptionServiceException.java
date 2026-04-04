package com.example.bankcards.exception;

public class EncryptionServiceException extends RuntimeException {
    public EncryptionServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
