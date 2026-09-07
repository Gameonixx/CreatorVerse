package com.creatorverse.campaign.application.service;

import com.creatorverse.campaign.application.dto.ApplicationCreateRequest;
import com.creatorverse.campaign.application.entity.CampaignApplication;
import com.creatorverse.campaign.application.entity.enums.ApplicationStatus;
import com.creatorverse.campaign.application.repository.CampaignApplicationRepository;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.campaign.entity.enums.CampaignStatus;
import com.creatorverse.campaign.repository.CampaignRepository;
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
}
