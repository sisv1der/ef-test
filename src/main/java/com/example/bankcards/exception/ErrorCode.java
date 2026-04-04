package com.example.bankcards.exception;

public enum ErrorCode {
    JWT_FAILED("JWT_FAILED"),
    BAD_CREDENTIALS("BAD_CREDENTIALS");

    private final String name;

    ErrorCode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
