package com.creatorverse.campaign.service;

import com.creatorverse.brand.entity.BrandProfile;
import com.creatorverse.brand.repository.BrandProfileRepository;
import com.creatorverse.campaign.application.repository.CampaignApplicationRepository;
import com.creatorverse.campaign.dto.CampaignCreateRequest;
import com.creatorverse.campaign.dto.CampaignResponse;
import com.creatorverse.campaign.dto.CampaignUpdateRequest;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.campaign.entity.enums.CampaignStatus;
import com.creatorverse.campaign.repository.CampaignRepository;
import com.creatorverse.campaign.repository.CampaignSpecification;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import com.creatorverse.collaboration.repository.CollaborationRepository;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;
    
    @Mock
    private BrandProfileRepository brandProfileRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CampaignApplicationRepository applicationRepository;

    @Mock
    private CollaborationRepository collaborationRepository;

    @InjectMocks
    private CampaignService campaignService;

    private User user;
    private BrandProfile brandProfile;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setDisplayName("Test Brand");

        brandProfile = new BrandProfile();
        brandProfile.setUser(user);
        brandProfile.setCompanyName("Test Company");

        campaign = new Campaign();
        campaign.setId(100L);
        campaign.setTitle("Test Campaign");
        campaign.setBrandUser(user);
    }

    @Test
    void testCreateCampaign_WithBrandProfile_Success() {
        CampaignCreateRequest req = new CampaignCreateRequest();
        req.setTitle("Test Campaign");
        req.setBudget(new BigDecimal("500.00"));

        when(brandProfileRepository.findByUserId(1L)).thenReturn(Optional.of(brandProfile));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(i -> {
            Campaign c = i.getArgument(0);
            c.setId(100L);
            return c;
        });

        CampaignResponse response = campaignService.createCampaign(1L, req);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Test Campaign", response.getTitle());
        assertEquals(CampaignStatus.DRAFT, response.getStatus());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    void testCreateCampaign_WithoutBrandProfile_ThrowsException() {
        CampaignCreateRequest req = new CampaignCreateRequest();
        when(brandProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> campaignService.createCampaign(1L, req));
    }

    @Test
    void testUpdateCampaign_ActiveCollaboration_TermsMutation_ThrowsException() {
        CampaignUpdateRequest req = new CampaignUpdateRequest();
        req.setBudget(java.math.BigDecimal.valueOf(5000));

        when(campaignRepository.findById(100L)).thenReturn(Optional.of(campaign));
        when(collaborationRepository.existsByCampaignAndStatus(campaign, CollaborationStatus.ACTIVE)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> campaignService.updateCampaign(1L, 100L, req), "Cannot modify campaign terms while an ACTIVE collaboration exists.");
    }

    @Test
    void testUpdateCampaign_ActiveCollaboration_StatusUpdate_Success() {
        campaign.setStatus(CampaignStatus.OPEN);
        CampaignUpdateRequest req = new CampaignUpdateRequest();
        req.setStatus(CampaignStatus.CLOSED);

        when(campaignRepository.findById(100L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));
        when(brandProfileRepository.findByUserId(1L)).thenReturn(Optional.of(brandProfile));
        // termsMutated is false because only status is changed, so existsByCampaignAndStatus won't be called

        CampaignResponse res = campaignService.updateCampaign(1L, 100L, req);

        assertEquals(CampaignStatus.CLOSED, res.getStatus());
    }
}
