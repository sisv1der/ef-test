package com.example.bankcards.repository;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> hasUsername(String username) {
        if (username == null) return (r, cq, cb) -> null;
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("username"), username);
    }

    public static Specification<User> hasRole(Role role) {
        if (role == null) return (r, cq, cb) -> null;
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<User> hasActive(Boolean active) {
        if (active == null) return (r, cq, cb) -> null;
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isActive"),  active);
    }
}
