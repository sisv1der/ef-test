package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.dto.EncryptedRecord;
import com.example.bankcards.util.EncryptionService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class CardNumberEncryptionService {

    private final EncryptionService encryptionService;

    public CardNumberEncryptionService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public EncryptedRecord encryptCardNumber(String cardNumber) {
        String iv = encryptionService.generateIv();
        String salt = encryptionService.generateSalt();
        SecretKey key = encryptionService.generateSecretKey(salt);

        String cipherText = encryptionService.encrypt(cardNumber, key, iv);

        return new EncryptedRecord(salt, iv, cipherText);
    }

    public String decryptCardNumber(Card card) {
        SecretKey key = encryptionService.generateSecretKey(card.getSalt());
        return encryptionService.decrypt(card.getEncryptedNumber(), key, card.getIv());
    }
}
