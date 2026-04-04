package com.example.bankcards.util;

public record EncryptedRecord(String salt, String iv, String cipherText) {
}
