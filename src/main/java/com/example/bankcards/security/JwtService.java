package com.example.bankcards.security;

import com.example.bankcards.config.JwtProperties;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final MACSigner signer;

    public JwtService(JwtProperties jwtProperties) throws KeyLengthException {
        this.jwtProperties = jwtProperties;
        this.signer = new MACSigner(getSecretKey());
    }

    private byte[] getSecretKey() {
        return jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(UserDetails userDetails) throws JOSEException {
        long now = System.currentTimeMillis();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .jwtID(UUID.randomUUID().toString())
                .issuer("BankCards")
                .issueTime(new Date(now))
                .expirationTime(new Date(now + jwtProperties.getExpiration()))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public boolean verifyToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean validSignature = signedJWT.verify(new MACVerifier(getSecretKey()));

        Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean isNotExpired = expirationDate != null && new Date().before(expirationDate);

        return validSignature && isNotExpired;
    }

    public String extractUsername(String token) throws ParseException {
        return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
    }
}
