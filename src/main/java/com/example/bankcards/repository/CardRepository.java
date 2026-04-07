package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    boolean existsByIdAndOwnerUsername(UUID cardId, String username);

    Page<Card> findByOwnerUsername(Pageable pageable, String username);

    Page<Card> findByOwnerUsernameAndStatus(Pageable pageable, String username, CardStatus status);

    Page<Card> findByStatus(Pageable pageable, CardStatus status);
}
