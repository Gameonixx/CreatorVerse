package com.creatorverse.discovery.service;

import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.discovery.dto.CreatorCardResponse;
import com.creatorverse.discovery.repository.CreatorDiscoverySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatorDiscoveryService {

    private final CreatorProfileRepository creatorProfileRepository;

    public CreatorDiscoveryService(CreatorProfileRepository creatorProfileRepository) {
        this.creatorProfileRepository = creatorProfileRepository;
    }

    @Transactional(readOnly = true)
    public Page<CreatorCardResponse> discoverCreators(String search, String niche, Integer minFollowers, Integer maxFollowers, Pageable pageable) {
        Specification<CreatorProfile> spec = CreatorDiscoverySpecification.withFilters(search, niche, minFollowers, maxFollowers);
        
        Page<CreatorProfile> profiles = creatorProfileRepository.findAll(spec, pageable);
        
        return profiles.map(this::mapToCardResponse);
    }

    private CreatorCardResponse mapToCardResponse(CreatorProfile profile) {
        return new CreatorCardResponse(
                profile.getUser().getId(),
                profile.getUser().getUsername(),
                profile.getUser().getDisplayName(),
                profile.getUser().getAvatarUrl(),
                profile.getUser().getBio(),
                profile.getUser().getFollowerCount(),
                profile.getNiche(),
                profile.getEngagementRate()
        );
    }
}
