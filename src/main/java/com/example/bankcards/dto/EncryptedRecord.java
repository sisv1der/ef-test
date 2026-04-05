package com.example.bankcards.dto;

public record EncryptedRecord(String salt, String iv, String cipherText) {
}
