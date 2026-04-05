package com.example.bankcards.security;

import com.example.bankcards.config.JwtProperties;
import com.example.bankcards.exception.JwtServiceException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final JwtProperties jwtProperties;
    private final MACSigner signer;
    private final MACVerifier verifier;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        try {
            this.signer = new MACSigner(getSecretKey());
            this.verifier = new MACVerifier(getSecretKey());
        } catch (JOSEException e) {
            log.error("failed to create JwtService bean", e);
            throw new JwtServiceException("failed to create JwtService bean", e);
        }
    }

    private byte[] getSecretKey() {
        return jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(UserDetails userDetails) {
        long now = System.currentTimeMillis();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .jwtID(UUID.randomUUID().toString())
                .issuer("BankCards")
                .issueTime(new Date(now))
                .expirationTime(new Date(now + jwtProperties.getExpiration()))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaimsSet);
        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            log.warn("Failed to generate token", e);
            throw new JwtServiceException("Failed to generate token", e);
        }

        return signedJWT.serialize();
    }

    public boolean verifyToken(String token) {
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            log.warn("Failed to parse token", e);
            throw new JwtServiceException("Failed to parse token", e);
        }

        boolean validSignature;
        try {
            validSignature = "BankCards".equals(signedJWT.getJWTClaimsSet().getIssuer()) && signedJWT.verify(verifier);
        } catch (JOSEException | ParseException e) {
            log.warn("Failed to verify token", e);
            throw new JwtServiceException("Failed to verify token", e);
        }

        Date expirationDate;
        try {
            expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            log.warn("Failed to parse expiration date", e);
            throw new JwtServiceException("Failed to parse expirationDate", e);
        }
        boolean isNotExpired = expirationDate != null && new Date().before(expirationDate);

        return validSignature && isNotExpired;
    }

    public String extractUsername(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            log.warn("Failed to parse token", e);
            throw new JwtServiceException("Failed to parse token", e);
        }
    }
}
