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

    @Column(unique = true, nullable = false)
    private String numberHash;

    @Column(nullable = false, length = 4)
    private String lastDigits;

    @Column(nullable = false)
    private BigDecimal balance;

    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private Instant expirationDate;

    public Card() {
    }

    public Card(UUID id, User owner, String numberHash, String lastDigits, BigDecimal balance, CardStatus status, Instant expirationDate) {
        this.id = id;
        this.owner = owner;
        this.numberHash = numberHash;
        this.lastDigits = lastDigits;
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

    public String getNumberHash() {
        return numberHash;
    }

    public void setNumberHash(String numberHash) {
        this.numberHash = numberHash;
    }

    public String getLastDigits() {
        return lastDigits;
    }

    public void setLastDigits(String lastDigits) {
        this.lastDigits = lastDigits;
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
        this.numberHash = builder.numberHash;
        this.lastDigits = builder.lastDigits;
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
        private String numberHash;
        private String lastDigits;
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

        public Builder numberHash(String numberHash) {
            this.numberHash = numberHash;
            return this;
        }

        public Builder lastDigits(String lastDigits) {
            this.lastDigits = lastDigits;
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
            if (id == null) throw new IllegalStateException("id is required");
            if (numberHash == null) throw new IllegalStateException("numberHash is required");
            if (lastDigits == null) throw new IllegalStateException("lastDigits is required");
            return new Card(this);
        }
    }
}
