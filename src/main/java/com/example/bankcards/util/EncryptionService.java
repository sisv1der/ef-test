package com.example.bankcards.util;

import com.example.bankcards.config.EncryptionProperties;
import com.example.bankcards.exception.EncryptionServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);
    private final EncryptionProperties encryptionProperties;
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    public EncryptionService(EncryptionProperties encryptionProperties) {
        this.encryptionProperties = encryptionProperties;
    }

    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String generateIv() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    public SecretKey generateSecretKey(String saltBase64) {
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm", e);
            throw new EncryptionServiceException("No such algorithm", e);
        }

        byte[] salt = Base64.getDecoder().decode(saltBase64);
        KeySpec spec = new PBEKeySpec(
                encryptionProperties.getSecretKey().toCharArray(),
                salt,
                65536,
                128
        );
        try {
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            log.error("Invalid key spec", e);
            throw new EncryptionServiceException("Invalid key spec", e);
        }
    }

    private GCMParameterSpec getGCMParameterSpec(String ivBase64) {
        byte[] iv = Base64.getDecoder().decode(ivBase64);

        return new GCMParameterSpec(128, iv);
    }

    public String encrypt(String input, SecretKey key, String iv) {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, key, iv);

        return encrypt(cipher, input);
    }

    private String encrypt(Cipher cipher, String input) {
        try {
            byte[] cipherText = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (IllegalBlockSizeException e) {
            log.error("Illegal block size", e);
            throw new EncryptionServiceException("Illegal block size", e);
        } catch (BadPaddingException e) {
            log.error("Bad padding", e);
            throw new EncryptionServiceException("Bad padding", e);
        }
    }

    public String decrypt(String cipherText, SecretKey key, String iv) {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, key, iv);
        return decrypt(cipher, cipherText);
    }

    private String decrypt(Cipher cipher, String cipherText) {
        byte[] plainText;
        try {
            plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalBlockSizeException e) {
            log.error("Illegal block size", e);
            throw new EncryptionServiceException("Illegal block size", e);
        } catch (BadPaddingException e) {
            log.error("Bad padding", e);
            throw new EncryptionServiceException("Bad padding", e);
        }
        return new String(plainText, StandardCharsets.UTF_8);
    }

    private Cipher getCipher(int opmode, SecretKey key, String iv) {
        Cipher cipher;

        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm", e);
            throw new EncryptionServiceException("No such algorithm", e);
        } catch (NoSuchPaddingException e) {
            log.error("No such padding", e);
            throw new EncryptionServiceException("No such padding", e);
        }

        try {
            cipher.init(opmode, key, getGCMParameterSpec(iv));
            return cipher;
        } catch (InvalidKeyException e) {
            log.error("Invalid key provided", e);
            throw new EncryptionServiceException("Invalid key provided", e);
        } catch (InvalidAlgorithmParameterException e) {
            log.error("Invalid IV provided", e);
            throw new EncryptionServiceException("Invalid IV provided", e);
        }
    }
}
