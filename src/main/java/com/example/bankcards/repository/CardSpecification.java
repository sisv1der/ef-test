package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.domain.Specification;

public class CardSpecification {

    private CardSpecification() {}

    public static Specification<Card> hasStatus (CardStatus status) {
        if (status == null) return (r, cq, cb) -> null;
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Card> hasOwnerUsername(String username) {
        if (username == null) return (r, cq, cb) -> null;
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("owner").get("username"), username);
    }
}
