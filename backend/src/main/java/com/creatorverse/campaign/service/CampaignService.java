package com.creatorverse.campaign.service;

import com.creatorverse.brand.entity.BrandProfile;
import com.creatorverse.brand.repository.BrandProfileRepository;
import com.creatorverse.campaign.dto.CampaignCreateRequest;
import com.creatorverse.campaign.dto.CampaignResponse;
import com.creatorverse.campaign.dto.CampaignUpdateRequest;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.campaign.entity.enums.CampaignStatus;
import com.creatorverse.campaign.repository.CampaignRepository;
import com.creatorverse.campaign.repository.CampaignSpecification;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final BrandProfileRepository brandProfileRepository;
    private final UserRepository userRepository;
    private final com.creatorverse.campaign.application.repository.CampaignApplicationRepository applicationRepository;

    public CampaignService(CampaignRepository campaignRepository, 
                           BrandProfileRepository brandProfileRepository,
                           UserRepository userRepository,
                           com.creatorverse.campaign.application.repository.CampaignApplicationRepository applicationRepository) {
        this.campaignRepository = campaignRepository;
        this.brandProfileRepository = brandProfileRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public CampaignResponse createCampaign(Long userId, CampaignCreateRequest request) {
        BrandProfile brandProfile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User does not have an active BrandProfile."));

        Campaign campaign = new Campaign();
        campaign.setBrandUser(brandProfile.getUser());
        campaign.setTitle(request.getTitle());
        campaign.setDescription(request.getDescription());
        campaign.setNiche(request.getNiche());
        campaign.setBudget(request.getBudget());
        campaign.setApplicationDeadline(request.getApplicationDeadline());
        campaign.setStatus(CampaignStatus.DRAFT);

        Campaign saved = campaignRepository.save(campaign);
        return mapToResponse(saved, brandProfile);
    }

    @Transactional
    public CampaignResponse updateCampaign(Long userId, Long campaignId, CampaignUpdateRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));

        if (!campaign.getBrandUser().getId().equals(userId)) {
            throw new SecurityException("User is not the owner of this campaign.");
        }

        if (request.getStatus() != null && campaign.getStatus() != request.getStatus()) {
            if (!campaign.getStatus().canTransitionTo(request.getStatus())) {
                throw new IllegalStateException("Invalid status transition from " + campaign.getStatus() + " to " + request.getStatus());
            }
            if (request.getStatus() == CampaignStatus.OPEN) {
                LocalDateTime deadline = request.getApplicationDeadline() != null ? request.getApplicationDeadline() : campaign.getApplicationDeadline();
                if (deadline == null) {
                    throw new IllegalStateException("An application deadline is required for an OPEN campaign.");
                }
            }
            campaign.setStatus(request.getStatus());
        }

        if (request.getTitle() != null) campaign.setTitle(request.getTitle());
        if (request.getDescription() != null) campaign.setDescription(request.getDescription());
        if (request.getNiche() != null) campaign.setNiche(request.getNiche());
        if (request.getBudget() != null) campaign.setBudget(request.getBudget());
        if (request.getApplicationDeadline() != null) campaign.setApplicationDeadline(request.getApplicationDeadline());

        Campaign saved = campaignRepository.save(campaign);
        
        BrandProfile brandProfile = brandProfileRepository.findByUserId(userId).orElseThrow();
        return mapToResponse(saved, brandProfile);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> discoverCampaigns(String search, String niche, Pageable pageable) {
        Specification<Campaign> spec = CampaignSpecification.withFilters(search, niche, CampaignStatus.OPEN);
        return mapCampaignsToResponse(campaignRepository.findAll(spec, pageable));
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> getMyCampaigns(Long userId, Pageable pageable) {
        return mapCampaignsToResponse(campaignRepository.findByBrandUser_Id(userId, pageable));
    }
    
    private Page<CampaignResponse> mapCampaignsToResponse(Page<Campaign> campaigns) {
        if (campaigns.isEmpty()) {
            return campaigns.map(c -> mapToResponse(c, null));
        }

        java.util.List<Long> userIds = campaigns.getContent().stream()
                .map(c -> c.getBrandUser().getId())
                .distinct()
                .toList();

        java.util.Map<Long, BrandProfile> profileMap = brandProfileRepository.findByUserIdIn(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.getUser().getId(), p -> p));

        return campaigns.map(c -> mapToResponse(c, profileMap.get(c.getBrandUser().getId())));
    }

    @Transactional(readOnly = true)
    public CampaignResponse getCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));
        return mapToResponse(campaign);
    }

    @Transactional
    public void deleteCampaign(Long userId, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));

        if (!campaign.getBrandUser().getId().equals(userId)) {
            throw new SecurityException("User is not the owner of this campaign.");
        }

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT campaigns can be hard deleted. Please CLOSE the campaign instead.");
        }

        if (applicationRepository.countByCampaign(campaign) > 0) {
            throw new IllegalStateException("Cannot delete a campaign that has applications. Please CLOSE it instead.");
        }
        
        campaignRepository.delete(campaign);
    }

    private CampaignResponse mapToResponse(Campaign campaign) {
        BrandProfile profile = brandProfileRepository.findByUserId(campaign.getBrandUser().getId()).orElse(null);
        return mapToResponse(campaign, profile);
    }

    private CampaignResponse mapToResponse(Campaign campaign, BrandProfile brandProfile) {
        CampaignResponse response = new CampaignResponse();
        response.setId(campaign.getId());
        response.setBrandUserId(campaign.getBrandUser().getId());
        
        if (brandProfile != null) {
            response.setBrandName(brandProfile.getCompanyName() != null ? brandProfile.getCompanyName() : campaign.getBrandUser().getDisplayName());
            response.setBrandLogoUrl(brandProfile.getLogoUrl() != null ? brandProfile.getLogoUrl() : campaign.getBrandUser().getAvatarUrl());
        } else {
            response.setBrandName(campaign.getBrandUser().getDisplayName());
            response.setBrandLogoUrl(campaign.getBrandUser().getAvatarUrl());
        }
        
        response.setTitle(campaign.getTitle());
        response.setDescription(campaign.getDescription());
        response.setNiche(campaign.getNiche());
        response.setBudget(campaign.getBudget());
        response.setStatus(campaign.getStatus());
        response.setApplicationDeadline(campaign.getApplicationDeadline());
        response.setCreatedAt(campaign.getCreatedAt());
        response.setUpdatedAt(campaign.getUpdatedAt());
        return response;
    }
}
