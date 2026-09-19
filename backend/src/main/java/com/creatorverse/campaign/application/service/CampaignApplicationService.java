package com.creatorverse.campaign.application.service;

import com.creatorverse.campaign.application.dto.ApplicationCreateRequest;
import com.creatorverse.campaign.application.dto.ApplicationResponse;
import com.creatorverse.campaign.application.dto.ApplicationReviewRequest;
import com.creatorverse.campaign.application.entity.CampaignApplication;
import com.creatorverse.campaign.application.entity.enums.ApplicationStatus;
import com.creatorverse.campaign.application.repository.CampaignApplicationRepository;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.campaign.entity.enums.CampaignStatus;
import com.creatorverse.campaign.repository.CampaignRepository;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.collaboration.service.CollaborationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CampaignApplicationService {

    private final CampaignApplicationRepository applicationRepository;
    private final CampaignRepository campaignRepository;
    private final CreatorProfileRepository creatorProfileRepository;
    private final CollaborationService collaborationService;

    public CampaignApplicationService(CampaignApplicationRepository applicationRepository,
                                      CampaignRepository campaignRepository,
                                      CreatorProfileRepository creatorProfileRepository,
                                      CollaborationService collaborationService) {
        this.applicationRepository = applicationRepository;
        this.campaignRepository = campaignRepository;
        this.creatorProfileRepository = creatorProfileRepository;
        this.collaborationService = collaborationService;
    }

    @Transactional
    public ApplicationResponse applyToCampaign(Long userId, Long campaignId, ApplicationCreateRequest request) {
        CreatorProfile creatorProfile = creatorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User does not have an active CreatorProfile."));

        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));

        if (campaign.getBrandUser().getId().equals(userId)) {
            throw new IllegalStateException("You cannot apply to your own campaign.");
        }

        if (campaign.getStatus() != CampaignStatus.OPEN) {
            throw new IllegalStateException("This campaign is not accepting applications.");
        }

        if (campaign.getApplicationDeadline() != null && LocalDateTime.now().isAfter(campaign.getApplicationDeadline())) {
            throw new IllegalStateException("The application deadline for this campaign has passed.");
        }

        if (applicationRepository.existsByCampaignAndCreatorUser(campaign, creatorProfile.getUser())) {
            throw new IllegalStateException("You have already applied to this campaign.");
        }

        CampaignApplication application = new CampaignApplication();
        application.setCampaign(campaign);
        application.setCreatorUser(creatorProfile.getUser());
        application.setMessage(request.getMessage());
        application.setStatus(ApplicationStatus.PENDING);

        CampaignApplication saved = applicationRepository.save(application);
        return mapToResponse(saved, creatorProfile);
    }

    @Transactional
    public void withdrawApplication(Long userId, Long applicationId) {
        CampaignApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));

        if (!application.getCreatorUser().getId().equals(userId)) {
            throw new SecurityException("User is not the owner of this application.");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING applications can be withdrawn.");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    @Transactional
    public ApplicationResponse reviewApplication(Long userId, Long campaignId, Long applicationId, ApplicationReviewRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));

        if (!campaign.getBrandUser().getId().equals(userId)) {
            throw new SecurityException("User is not the owner of this campaign.");
        }

        CampaignApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));

        if (!application.getCampaign().getId().equals(campaignId)) {
            throw new IllegalArgumentException("Application does not belong to this campaign.");
        }

        if (request.getStatus() != ApplicationStatus.ACCEPTED && request.getStatus() != ApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("Review status must be ACCEPTED or REJECTED.");
        }

        application.setStatus(request.getStatus());
        CampaignApplication saved = applicationRepository.save(application);

        if (saved.getStatus() == ApplicationStatus.ACCEPTED) {
            collaborationService.createCollaboration(saved);
        }

        CreatorProfile profile = creatorProfileRepository.findByUserId(saved.getCreatorUser().getId()).orElse(null);
        return mapToResponse(saved, profile);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getCampaignApplications(Long userId, Long campaignId, Pageable pageable) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));

        if (!campaign.getBrandUser().getId().equals(userId)) {
            throw new SecurityException("User is not the owner of this campaign.");
        }

        return mapApplicationsToResponse(applicationRepository.findByCampaign(campaign, pageable));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(Long userId, Pageable pageable) {
        CreatorProfile creatorProfile = creatorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User does not have an active CreatorProfile."));

        return mapApplicationsToResponse(applicationRepository.findByCreatorUser(creatorProfile.getUser(), pageable));
    }

    private Page<ApplicationResponse> mapApplicationsToResponse(Page<CampaignApplication> applications) {
        if (applications.isEmpty()) {
            return applications.map(a -> mapToResponse(a, null));
        }

        java.util.List<Long> userIds = applications.getContent().stream()
                .map(a -> a.getCreatorUser().getId())
                .distinct()
                .toList();

        java.util.Map<Long, CreatorProfile> profileMap = creatorProfileRepository.findByUserIdIn(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.getUser().getId(), p -> p));

        return applications.map(a -> mapToResponse(a, profileMap.get(a.getCreatorUser().getId())));
    }

    private ApplicationResponse mapToResponse(CampaignApplication application) {
        CreatorProfile profile = creatorProfileRepository.findByUserId(application.getCreatorUser().getId()).orElse(null);
        return mapToResponse(application, profile);
    }

    private ApplicationResponse mapToResponse(CampaignApplication application, CreatorProfile profile) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setCampaignId(application.getCampaign().getId());
        response.setCreatorUserId(application.getCreatorUser().getId());
        
        response.setCreatorName(application.getCreatorUser().getDisplayName());
        response.setCreatorAvatarUrl(application.getCreatorUser().getAvatarUrl());
        response.setCreatorFollowerCount(application.getCreatorUser().getFollowerCount());
        
        if (profile != null) {
            response.setCreatorEngagementRate(profile.getEngagementRate());
        }
        
        response.setMessage(application.getMessage());
        response.setStatus(application.getStatus());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }
}
