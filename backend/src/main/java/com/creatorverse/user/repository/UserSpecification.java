package com.creatorverse.user.repository;

import com.creatorverse.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class UserSpecification {

    public static Specification<User> withSearchQuery(String q) {
        return (root, query, criteriaBuilder) -> {
            if (q == null || q.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + q.trim().toLowerCase() + "%";
            
            Predicate usernameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchPattern);
            Predicate displayNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("displayName")), searchPattern);
            
            return criteriaBuilder.or(usernameMatch, displayNameMatch);
        };
    }
}
