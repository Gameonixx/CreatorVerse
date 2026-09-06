package com.creatorverse.discovery.repository;

import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CreatorDiscoverySpecification {

    public static Specification<CreatorProfile> withFilters(String search, String niche, Integer minFollowers, Integer maxFollowers) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Join User for filtering, use LEFT join to avoid issues if we need to fetch, 
            // but since it's OneToOne (nullable=false), INNER join is fine and more performant.
            Join<CreatorProfile, User> userJoin = root.join("user", JoinType.INNER);

            // Fetch user to avoid N+1 if it's a fetch query
            if (Long.class != query.getResultType()) {
                root.fetch("user", JoinType.INNER);
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate usernameMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("username")), searchPattern);
                Predicate displayNameMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("displayName")), searchPattern);
                Predicate nicheMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("niche")), searchPattern);
                predicates.add(criteriaBuilder.or(usernameMatch, displayNameMatch, nicheMatch));
            }

            if (niche != null && !niche.trim().isEmpty()) {
                String nichePattern = "%" + niche.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("niche")), nichePattern));
            }

            if (minFollowers != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(userJoin.get("followerCount"), minFollowers));
            }

            if (maxFollowers != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(userJoin.get("followerCount"), maxFollowers));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
