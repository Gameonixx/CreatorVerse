package com.creatorverse.collaboration.service;

import com.creatorverse.brand.entity.BrandProfile;
import com.creatorverse.brand.repository.BrandProfileRepository;
import com.creatorverse.campaign.application.entity.CampaignApplication;
import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.collaboration.dto.CollaborationResponse;
import com.creatorverse.collaboration.entity.Collaboration;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import com.creatorverse.collaboration.repository.CollaborationRepository;
import com.creatorverse.creator.entity.CreatorProfile;
import com.creatorverse.creator.repository.CreatorProfileRepository;
import com.creatorverse.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CollaborationServiceTest {

    @Mock
    private CollaborationRepository collaborationRepository;

    @Mock
    private CreatorProfileRepository creatorProfileRepository;

    @Mock
    private BrandProfileRepository brandProfileRepository;

    @InjectMocks
    private CollaborationService collaborationService;

    private User creatorUser;
    private User brandUser;
    private User otherUser;
    private Campaign campaign;
    private CampaignApplication application;
    private Collaboration collaboration;

    @BeforeEach
    void setUp() {
        creatorUser = new User();
        creatorUser.setId(1L);
        creatorUser.setUsername("creator");

        brandUser = new User();
        brandUser.setId(2L);
        brandUser.setUsername("brand");

        otherUser = new User();
        otherUser.setId(3L);
        otherUser.setUsername("other");

        campaign = new Campaign();
        campaign.setId(10L);
        campaign.setBrandUser(brandUser);

        application = new CampaignApplication();
        application.setId(100L);
        application.setCampaign(campaign);
        application.setCreatorUser(creatorUser);

        collaboration = new Collaboration();
        collaboration.setId(1000L);
        collaboration.setCampaign(campaign);
        collaboration.setCreatorUser(creatorUser);
        collaboration.setOriginatingApplication(application);
        collaboration.setStatus(CollaborationStatus.ACTIVE);
    }

    @Test
    void createCollaboration_Success() {
        when(collaborationRepository.existsByOriginatingApplication(application)).thenReturn(false);
        when(collaborationRepository.saveAndFlush(any(Collaboration.class))).thenAnswer(i -> {
            Collaboration c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        Collaboration result = collaborationService.createCollaboration(application);

        assertNotNull(result);
        assertEquals(CollaborationStatus.ACTIVE, result.getStatus());
        assertEquals(application, result.getOriginatingApplication());
    }

    @Test
    void createCollaboration_DuplicateExists_ThrowsException() {
        when(collaborationRepository.existsByOriginatingApplication(application)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> collaborationService.createCollaboration(application));
    }

    @Test
    void createCollaboration_DataIntegrityViolation_ThrowsException() {
        when(collaborationRepository.existsByOriginatingApplication(application)).thenReturn(false);
        when(collaborationRepository.saveAndFlush(any(Collaboration.class))).thenThrow(new DataIntegrityViolationException("Duplicate"));

        assertThrows(IllegalStateException.class, () -> collaborationService.createCollaboration(application));
    }

    @Test
    void updateStatus_CreatorCanCancel() {
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));
        when(collaborationRepository.save(any(Collaboration.class))).thenReturn(collaboration);

        CollaborationResponse response = collaborationService.updateStatus(1000L, creatorUser.getId(), CollaborationStatus.CANCELLED);

        assertEquals(CollaborationStatus.CANCELLED, response.getStatus());
    }

    @Test
    void updateStatus_BrandCanCancel() {
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));
        when(collaborationRepository.save(any(Collaboration.class))).thenReturn(collaboration);

        CollaborationResponse response = collaborationService.updateStatus(1000L, brandUser.getId(), CollaborationStatus.CANCELLED);

        assertEquals(CollaborationStatus.CANCELLED, response.getStatus());
    }

    @Test
    void updateStatus_BrandCanComplete() {
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));
        when(collaborationRepository.save(any(Collaboration.class))).thenReturn(collaboration);

        CollaborationResponse response = collaborationService.updateStatus(1000L, brandUser.getId(), CollaborationStatus.COMPLETED);

        assertEquals(CollaborationStatus.COMPLETED, response.getStatus());
    }

    @Test
    void updateStatus_CreatorCannotComplete_ThrowsException() {
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));

        assertThrows(SecurityException.class, () -> collaborationService.updateStatus(1000L, creatorUser.getId(), CollaborationStatus.COMPLETED));
    }

    @Test
    void updateStatus_UnauthorizedUser_ThrowsException() {
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));

        assertThrows(SecurityException.class, () -> collaborationService.updateStatus(1000L, otherUser.getId(), CollaborationStatus.CANCELLED));
    }

    @Test
    void updateStatus_FromCompleted_ThrowsException() {
        collaboration.setStatus(CollaborationStatus.COMPLETED);
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));

        assertThrows(IllegalStateException.class, () -> collaborationService.updateStatus(1000L, brandUser.getId(), CollaborationStatus.CANCELLED));
    }

    @Test
    void updateStatus_FromCancelled_ThrowsException() {
        collaboration.setStatus(CollaborationStatus.CANCELLED);
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));

        assertThrows(IllegalStateException.class, () -> collaborationService.updateStatus(1000L, brandUser.getId(), CollaborationStatus.COMPLETED));
    }

    @Test
    void getCollaboration_CreatorCanView() {
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));

        CollaborationResponse response = collaborationService.getCollaboration(1000L, creatorUser.getId());

        assertNotNull(response);
        assertEquals(1000L, response.getId());
    }

    @Test
    void getCollaboration_BrandCanView() {
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));

        CollaborationResponse response = collaborationService.getCollaboration(1000L, brandUser.getId());

        assertNotNull(response);
        assertEquals(1000L, response.getId());
    }

    @Test
    void getCollaboration_Unauthorized_ThrowsException() {
        when(collaborationRepository.findById(1000L)).thenReturn(Optional.of(collaboration));

        assertThrows(SecurityException.class, () -> collaborationService.getCollaboration(1000L, otherUser.getId()));
    }

    @Test
    void getMyCollaborations_DualContextUser_ReturnsBoth() {
        // User acts as creator in collab1 and brand in collab2
        User dualContextUser = new User();
        dualContextUser.setId(4L);

        Collaboration collabAsCreator = new Collaboration();
        collabAsCreator.setId(1001L);
        collabAsCreator.setCreatorUser(dualContextUser);
        collabAsCreator.setCampaign(campaign);
        collabAsCreator.setOriginatingApplication(application);

        Campaign campaignByDualUser = new Campaign();
        campaignByDualUser.setBrandUser(dualContextUser);
        
        Collaboration collabAsBrand = new Collaboration();
        collabAsBrand.setId(1002L);
        collabAsBrand.setCreatorUser(creatorUser);
        collabAsBrand.setCampaign(campaignByDualUser);
        collabAsBrand.setOriginatingApplication(application);

        Page<Collaboration> page = new PageImpl<>(List.of(collabAsCreator, collabAsBrand));

        when(collaborationRepository.findByCreatorUser_IdOrCampaign_BrandUser_Id(eq(4L), eq(4L), any(Pageable.class)))
                .thenReturn(page);

        Page<CollaborationResponse> responsePage = collaborationService.getMyCollaborations(4L, PageRequest.of(0, 10));

        assertEquals(2, responsePage.getTotalElements());
        assertTrue(responsePage.getContent().stream().anyMatch(r -> r.getId().equals(1001L)));
        assertTrue(responsePage.getContent().stream().anyMatch(r -> r.getId().equals(1002L)));
    }
}
