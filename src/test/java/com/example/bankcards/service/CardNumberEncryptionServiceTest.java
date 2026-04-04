package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.util.EncryptedRecord;
import com.example.bankcards.util.EncryptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardNumberEncryptionServiceTest {

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardNumberEncryptionService cardNumberEncryptionService;

    @Test
    void encryptCardNumber() {
        String cardNumber = "1234567890123456";
        String iv = "123456789";
        String salt = "123456789";
        SecretKey secretKey = mock(SecretKey.class);
        String cipherText = "encrypted";
        when(encryptionService.generateIv()).thenReturn(iv);
        when(encryptionService.generateSalt()).thenReturn(salt);
        when(encryptionService.generateSecretKey(salt)).thenReturn(secretKey);
        when(encryptionService.encrypt(cardNumber, secretKey, iv)).thenReturn(cipherText);

        EncryptedRecord result = cardNumberEncryptionService.encryptCardNumber(cardNumber);

        assertNotNull(result);
        assertEquals(cipherText, result.cipherText());
        assertEquals(salt, result.salt());
        assertEquals(iv, result.iv());

        verify(encryptionService).generateIv();
        verify(encryptionService).generateSalt();
        verify(encryptionService).generateSecretKey(salt);
        verify(encryptionService).encrypt(cardNumber, secretKey, iv);
    }

    @Test
    void decryptCardNumber() {
        String cardNumber = "1234567890123456";
        String iv = "123456789";
        String salt = "123456789";
        SecretKey secretKey = mock(SecretKey.class);
        String cipherText = "encrypted";

        Card card = new Card();
        card.setSalt(salt);
        card.setIv(iv);
        card.setEncryptedNumber(cipherText);

        when(encryptionService.generateSecretKey(salt)).thenReturn(secretKey);
        when(encryptionService.decrypt(cipherText, secretKey, iv)).thenReturn(cardNumber);

        String result = cardNumberEncryptionService.decryptCardNumber(card);

        assertNotNull(result);
        assertEquals(cardNumber, result);

        verify(encryptionService).generateSecretKey(salt);
        verify(encryptionService).decrypt(cipherText, secretKey, iv);
    }
}