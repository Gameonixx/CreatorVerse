package com.creatorverse.campaign.application.service;

import com.creatorverse.campaign.application.dto.ApplicationCreateRequest;
import com.creatorverse.campaign.application.entity.CampaignApplication;
import com.creatorverse.campaign.application.entity.enums.ApplicationStatus;
import com.creatorverse.campaign.application.repository.CampaignApplicationRepository;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.campaign.entity.enums.CampaignStatus;
import com.creatorverse.campaign.repository.CampaignRepository;
import com.creatorverse.collaboration.service.CollaborationService;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignApplicationServiceTest {

    @Mock
    private CampaignApplicationRepository applicationRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CreatorProfileRepository creatorProfileRepository;

    @Mock
    private CollaborationService collaborationService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CampaignApplicationService applicationService;

    private User brandUser;
    private User creatorUser;
    private CreatorProfile creatorProfile;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        brandUser = new User();
        brandUser.setId(1L);

        creatorUser = new User();
        creatorUser.setId(2L);

        creatorProfile = new CreatorProfile();
        creatorProfile.setUser(creatorUser);

        campaign = new Campaign();
        campaign.setId(10L);
        campaign.setBrandUser(brandUser);
        campaign.setStatus(CampaignStatus.OPEN);
    }

    @Test
    void testApplyToCampaign_Success() {
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        req.setMessage("Hire me!");

        when(creatorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(creatorProfile));
        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));
        when(applicationRepository.existsByCampaignAndCreatorUser(campaign, creatorUser)).thenReturn(false);
        when(applicationRepository.save(any(CampaignApplication.class))).thenAnswer(i -> {
            CampaignApplication a = i.getArgument(0);
            a.setId(100L);
            return a;
        });

        var res = applicationService.applyToCampaign(2L, 10L, req);

        assertNotNull(res);
        assertEquals(ApplicationStatus.PENDING, res.getStatus());
    }

    @Test
    void testApplyToCampaign_DualContext_OwnCampaign_ThrowsException() {
        // User 1 has both BrandProfile and CreatorProfile
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        
        CreatorProfile profile1 = new CreatorProfile();
        profile1.setUser(brandUser);

        when(creatorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile1));
        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign)); // campaign belongs to brandUser (ID=1)

        assertThrows(IllegalStateException.class, () -> applicationService.applyToCampaign(1L, 10L, req), "You cannot apply to your own campaign.");
    }

    @Test
    void testReviewApplication_Accepted_CreatesCollaboration() {
        com.creatorverse.campaign.application.dto.ApplicationReviewRequest req = new com.creatorverse.campaign.application.dto.ApplicationReviewRequest();
        req.setStatus(ApplicationStatus.ACCEPTED);

        CampaignApplication app = new CampaignApplication();
        app.setId(100L);
        app.setCampaign(campaign);
        app.setCreatorUser(creatorUser);
        app.setStatus(ApplicationStatus.PENDING);

        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(CampaignApplication.class))).thenAnswer(i -> i.getArgument(0));
        when(collaborationService.createCollaboration(any(CampaignApplication.class))).thenAnswer(i -> {
            com.creatorverse.collaboration.entity.Collaboration collab = new com.creatorverse.collaboration.entity.Collaboration();
            collab.setId(101L);
            return collab;
        });

        applicationService.reviewApplication(1L, 10L, 100L, req);

        verify(applicationRepository).save(app);
        assertEquals(ApplicationStatus.ACCEPTED, app.getStatus());
        verify(collaborationService, times(1)).createCollaboration(app);
    }

    @Test
    void testReviewApplication_Rejected_DoesNotCreateCollaboration() {
        com.creatorverse.campaign.application.dto.ApplicationReviewRequest req = new com.creatorverse.campaign.application.dto.ApplicationReviewRequest();
        req.setStatus(ApplicationStatus.REJECTED);

        CampaignApplication app = new CampaignApplication();
        app.setId(100L);
        app.setCampaign(campaign);
        app.setCreatorUser(creatorUser);
        app.setStatus(ApplicationStatus.PENDING);

        when(campaignRepository.findById(10L)).thenReturn(Optional.of(campaign));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(CampaignApplication.class))).thenAnswer(i -> i.getArgument(0));

        applicationService.reviewApplication(1L, 10L, 100L, req);

        verify(applicationRepository).save(app);
        assertEquals(ApplicationStatus.REJECTED, app.getStatus());
        verify(collaborationService, never()).createCollaboration(any());
    }
}
