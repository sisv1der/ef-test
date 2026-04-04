package com.example.bankcards.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
public class Card {

    @Id
    @Column(unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String ownerName;

    @Column(unique = true, nullable = false)
    private String encryptedNumber;

    @Column(nullable = false)
    private String salt;

    @Column(nullable = false)
    private String iv;

    @Column(nullable = false)
    private BigDecimal balance;

    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private Instant expirationDate;

    public Card() {
    }

    public Card(UUID id, User owner, String ownerName, String encryptedNumber, String salt, String iv, BigDecimal balance, CardStatus status, Instant expirationDate) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.encryptedNumber = encryptedNumber;
        this.salt = salt;
        this.iv = iv;
        this.balance = balance;
        this.status = status;
        this.expirationDate = expirationDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getEncryptedNumber() {
        return encryptedNumber;
    }

    public void setEncryptedNumber(String numberHash) {
        this.encryptedNumber = numberHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Card(Builder builder) {
        this.id = builder.id;
        this.owner = builder.owner;
        this.ownerName = builder.ownerName;
        this.encryptedNumber = builder.encryptedNumber;
        this.salt = builder.salt;
        this.iv = builder.iv;
        this.balance = builder.balance != null ? builder.balance : BigDecimal.ZERO;
        this.status = builder.status != null ? builder.status : CardStatus.ACTIVE;
        this.expirationDate = builder.expirationDate != null ? builder.expirationDate : Instant.now().plus(10, ChronoUnit.YEARS);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User owner;
        private String ownerName;
        private String encryptedNumber;
        private String salt;
        private String iv;
        private BigDecimal balance;
        private CardStatus status;
        private Instant expirationDate;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public Builder ownerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }

        public Builder encryptedNumber(String encryptedNumber) {
            this.encryptedNumber = encryptedNumber;
            return this;
        }

        public Builder salt(String salt) {
            this.salt = salt;
            return this;
        }

        public Builder iv(String iv) {
            this.iv = iv;
            return this;
        }

        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder status(CardStatus status) {
            this.status = status;
            return this;
        }

        public Builder expirationDate(Instant expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }

        public Card build() {
            if (owner == null) throw new IllegalStateException("user is required");
            if (ownerName == null) throw new IllegalStateException("owner is required");
            if (id == null) throw new IllegalStateException("id is required");
            if (salt == null) throw new IllegalStateException("salt is required");
            if (iv == null) throw new IllegalStateException("iv is required");
            if (encryptedNumber == null) throw new IllegalStateException("numberHash is required");
            return new Card(this);
        }
    }
}
