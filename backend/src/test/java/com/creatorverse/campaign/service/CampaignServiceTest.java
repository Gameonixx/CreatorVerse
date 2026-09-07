package com.creatorverse.campaign.service;

import com.creatorverse.brand.entity.BrandProfile;
import com.creatorverse.brand.repository.BrandProfileRepository;
import com.creatorverse.campaign.application.repository.CampaignApplicationRepository;
import com.creatorverse.campaign.dto.CampaignCreateRequest;
import com.creatorverse.campaign.dto.CampaignResponse;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.campaign.entity.enums.CampaignStatus;
import com.creatorverse.campaign.repository.CampaignRepository;
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

    @InjectMocks
    private CampaignService campaignService;

    private User user;
    private BrandProfile brandProfile;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setDisplayName("Test Brand");

        brandProfile = new BrandProfile();
        brandProfile.setUser(user);
        brandProfile.setCompanyName("Test Company");
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
}
