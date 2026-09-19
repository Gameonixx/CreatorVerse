package com.creatorverse.collaboration.service;

import com.creatorverse.brand.entity.BrandProfile;
import com.creatorverse.brand.repository.BrandProfileRepository;
import com.creatorverse.campaign.application.entity.CampaignApplication;
import com.creatorverse.collaboration.dto.CollaborationResponse;
import com.creatorverse.collaboration.entity.Collaboration;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import com.creatorverse.collaboration.repository.CollaborationRepository;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final CreatorProfileRepository creatorProfileRepository;
    private final BrandProfileRepository brandProfileRepository;

    public CollaborationService(CollaborationRepository collaborationRepository,
                                CreatorProfileRepository creatorProfileRepository,
                                BrandProfileRepository brandProfileRepository) {
        this.collaborationRepository = collaborationRepository;
        this.creatorProfileRepository = creatorProfileRepository;
        this.brandProfileRepository = brandProfileRepository;
    }

    @Transactional
    public Collaboration createCollaboration(CampaignApplication application) {
        if (collaborationRepository.existsByOriginatingApplication(application)) {
            // Already exists, return it or just ignore. Wait, the prompt says "do not create another"
            // Returning the existing one would require fetching it, but throwing avoids the duplicate.
            throw new IllegalStateException("Collaboration already exists for this application.");
        }

        Collaboration collaboration = new Collaboration();
        collaboration.setCampaign(application.getCampaign());
        collaboration.setCreatorUser(application.getCreatorUser());
        collaboration.setOriginatingApplication(application);
        collaboration.setStatus(CollaborationStatus.ACTIVE);

        try {
            return collaborationRepository.saveAndFlush(collaboration);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("A collaboration was concurrently created for this application.", e);
        }
    }

    @Transactional
    public CollaborationResponse updateStatus(Long id, Long userId, CollaborationStatus newStatus) {
        Collaboration collaboration = collaborationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collaboration not found."));

        boolean isCreator = collaboration.getCreatorUser().getId().equals(userId);
        boolean isBrand = collaboration.getCampaign().getBrandUser().getId().equals(userId);

        if (!isCreator && !isBrand) {
            throw new SecurityException("User is not authorized to access this collaboration.");
        }

        if (collaboration.getStatus() == CollaborationStatus.COMPLETED || collaboration.getStatus() == CollaborationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot transition from a terminal state.");
        }

        if (newStatus == CollaborationStatus.COMPLETED) {
            if (!isBrand) {
                throw new SecurityException("Only the brand owner can complete a collaboration.");
            }
        } else if (newStatus == CollaborationStatus.CANCELLED) {
            // Both can cancel
        } else {
            throw new IllegalArgumentException("Invalid status transition.");
        }

        collaboration.setStatus(newStatus);
        Collaboration saved = collaborationRepository.save(collaboration);
        return mapToResponse(saved, null, null);
    }

    @Transactional(readOnly = true)
    public CollaborationResponse getCollaboration(Long id, Long userId) {
        Collaboration collaboration = collaborationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collaboration not found."));

        boolean isCreator = collaboration.getCreatorUser().getId().equals(userId);
        boolean isBrand = collaboration.getCampaign().getBrandUser().getId().equals(userId);

        if (!isCreator && !isBrand) {
            throw new SecurityException("User is not authorized to access this collaboration.");
        }

        CreatorProfile creatorProfile = creatorProfileRepository.findByUserId(collaboration.getCreatorUser().getId()).orElse(null);
        BrandProfile brandProfile = brandProfileRepository.findByUserId(collaboration.getCampaign().getBrandUser().getId()).orElse(null);

        return mapToResponse(collaboration, creatorProfile, brandProfile);
    }

    @Transactional(readOnly = true)
    public Page<CollaborationResponse> getMyCollaborations(Long userId, Pageable pageable) {
        Page<Collaboration> collaborations = collaborationRepository.findByCreatorUser_IdOrCampaign_BrandUser_Id(userId, userId, pageable);
        return mapCollaborationsToResponse(collaborations);
    }

    private Page<CollaborationResponse> mapCollaborationsToResponse(Page<Collaboration> collaborations) {
        if (collaborations.isEmpty()) {
            return collaborations.map(c -> mapToResponse(c, null, null));
        }

        List<Long> creatorIds = collaborations.getContent().stream()
                .map(c -> c.getCreatorUser().getId())
                .distinct()
                .collect(Collectors.toList());

        List<Long> brandIds = collaborations.getContent().stream()
                .map(c -> c.getCampaign().getBrandUser().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, CreatorProfile> creatorProfiles = creatorProfileRepository.findByUserIdIn(creatorIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p));

        Map<Long, BrandProfile> brandProfiles = brandProfileRepository.findByUserIdIn(brandIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p));

        return collaborations.map(c -> mapToResponse(
                c,
                creatorProfiles.get(c.getCreatorUser().getId()),
                brandProfiles.get(c.getCampaign().getBrandUser().getId())
        ));
    }

    private CollaborationResponse mapToResponse(Collaboration collaboration, CreatorProfile creatorProfile, BrandProfile brandProfile) {
        CollaborationResponse response = new CollaborationResponse();
        response.setId(collaboration.getId());
        response.setCampaignId(collaboration.getCampaign().getId());
        response.setCampaignTitle(collaboration.getCampaign().getTitle());
        response.setCampaignDescription(collaboration.getCampaign().getDescription());
        response.setCampaignNiche(collaboration.getCampaign().getNiche());
        response.setCampaignBudget(collaboration.getCampaign().getBudget());
        response.setOriginatingApplicationId(collaboration.getOriginatingApplication().getId());
        response.setStatus(collaboration.getStatus());
        response.setCreatedAt(collaboration.getCreatedAt());
        response.setUpdatedAt(collaboration.getUpdatedAt());

        response.setCreatorUserId(collaboration.getCreatorUser().getId());
        response.setCreatorName(collaboration.getCreatorUser().getDisplayName());
        response.setCreatorAvatarUrl(collaboration.getCreatorUser().getAvatarUrl());

        response.setBrandUserId(collaboration.getCampaign().getBrandUser().getId());
        if (brandProfile != null) {
            response.setBrandName(brandProfile.getCompanyName() != null ? brandProfile.getCompanyName() : collaboration.getCampaign().getBrandUser().getDisplayName());
            response.setBrandLogoUrl(brandProfile.getLogoUrl() != null ? brandProfile.getLogoUrl() : collaboration.getCampaign().getBrandUser().getAvatarUrl());
        } else {
            response.setBrandName(collaboration.getCampaign().getBrandUser().getDisplayName());
            response.setBrandLogoUrl(collaboration.getCampaign().getBrandUser().getAvatarUrl());
        }

        return response;
    }
}
