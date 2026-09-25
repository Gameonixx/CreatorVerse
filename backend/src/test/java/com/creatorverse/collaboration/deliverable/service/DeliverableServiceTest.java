package com.creatorverse.collaboration.deliverable.service;

import com.creatorverse.campaign.entity.Campaign;
import com.creatorverse.collaboration.deliverable.dto.*;
import com.creatorverse.collaboration.deliverable.entity.Deliverable;
import com.creatorverse.collaboration.deliverable.entity.enums.DeliverableStatus;
import com.creatorverse.collaboration.deliverable.repository.DeliverableRepository;
import com.creatorverse.collaboration.entity.Collaboration;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import com.creatorverse.collaboration.repository.CollaborationRepository;
import com.creatorverse.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliverableServiceTest {

    @Mock
    private DeliverableRepository deliverableRepository;

    @Mock
    private CollaborationRepository collaborationRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DeliverableService deliverableService;

    private User brandUser;
    private User creatorUser;
    private User unrelatedUser;
    private Campaign campaign;
    private Collaboration collaboration;
    private Deliverable deliverable;

    @BeforeEach
    void setUp() {
        brandUser = new User();
        brandUser.setId(1L);

        creatorUser = new User();
        creatorUser.setId(2L);

        unrelatedUser = new User();
        unrelatedUser.setId(3L);

        campaign = new Campaign();
        campaign.setId(10L);
        campaign.setBrandUser(brandUser);

        collaboration = new Collaboration();
        collaboration.setId(100L);
        collaboration.setCampaign(campaign);
        collaboration.setCreatorUser(creatorUser);
        collaboration.setStatus(CollaborationStatus.ACTIVE);

        deliverable = new Deliverable();
        deliverable.setId(1000L);
        deliverable.setCollaboration(collaboration);
        deliverable.setTitle("Test Video");
        deliverable.setStatus(DeliverableStatus.PENDING);
    }

    // --- CREATE ---

    @Test
    void brandCanCreate() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.save(any(Deliverable.class))).thenAnswer(i -> {
            Deliverable d = i.getArgument(0);
            d.setId(1001L);
            return d;
        });

        DeliverableCreateRequest req = new DeliverableCreateRequest();
        req.setTitle("New Title");
        
        DeliverableResponse res = deliverableService.createDeliverable(100L, 1L, req);
        assertEquals("New Title", res.getTitle());
        assertEquals(DeliverableStatus.PENDING, res.getStatus());
    }

    @Test
    void creatorCannotCreate() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        DeliverableCreateRequest req = new DeliverableCreateRequest();
        req.setTitle("New");
        assertThrows(SecurityException.class, () -> deliverableService.createDeliverable(100L, 2L, req));
    }

    @Test
    void unrelatedCannotCreate() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        DeliverableCreateRequest req = new DeliverableCreateRequest();
        req.setTitle("New");
        assertThrows(SecurityException.class, () -> deliverableService.createDeliverable(100L, 3L, req));
    }

    @Test
    void inactiveCollaborationRejectsCreation() {
        collaboration.setStatus(CollaborationStatus.COMPLETED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        DeliverableCreateRequest req = new DeliverableCreateRequest();
        req.setTitle("New");
        assertThrows(IllegalStateException.class, () -> deliverableService.createDeliverable(100L, 1L, req));
    }

    // --- EDIT ---

    @Test
    void brandCanEditPending() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));
        when(deliverableRepository.save(any(Deliverable.class))).thenAnswer(i -> i.getArgument(0));

        DeliverableUpdateRequest req = new DeliverableUpdateRequest();
        req.setTitle("Updated Title");
        
        DeliverableResponse res = deliverableService.updateDeliverable(100L, 1000L, 1L, req);
        assertEquals("Updated Title", res.getTitle());
    }

    @Test
    void brandCannotEditSubmitted() {
        deliverable.setStatus(DeliverableStatus.SUBMITTED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));

        DeliverableUpdateRequest req = new DeliverableUpdateRequest();
        req.setTitle("Updated Title");
        
        assertThrows(IllegalStateException.class, () -> deliverableService.updateDeliverable(100L, 1000L, 1L, req));
    }

    @Test
    void creatorCannotEdit() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        DeliverableUpdateRequest req = new DeliverableUpdateRequest();
        req.setTitle("Upd");
        assertThrows(SecurityException.class, () -> deliverableService.updateDeliverable(100L, 1000L, 2L, req));
    }

    // --- DELETE ---

    @Test
    void brandCanDeletePending() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));

        deliverableService.deleteDeliverable(100L, 1000L, 1L);
        verify(deliverableRepository, times(1)).delete(deliverable);
    }

    @Test
    void brandCannotDeleteSubmitted() {
        deliverable.setStatus(DeliverableStatus.SUBMITTED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));

        assertThrows(IllegalStateException.class, () -> deliverableService.deleteDeliverable(100L, 1000L, 1L));
    }

    @Test
    void creatorCannotDelete() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        assertThrows(SecurityException.class, () -> deliverableService.deleteDeliverable(100L, 1000L, 2L));
    }

    // --- SUBMISSION ---

    @Test
    void creatorCanSubmitPending() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));
        when(deliverableRepository.save(any(Deliverable.class))).thenAnswer(i -> i.getArgument(0));

        DeliverableSubmissionRequest req = new DeliverableSubmissionRequest();
        req.setSubmissionUrl("http://example.com");

        DeliverableResponse res = deliverableService.submitDeliverable(100L, 1000L, 2L, req);
        assertEquals(DeliverableStatus.SUBMITTED, res.getStatus());
        assertEquals("http://example.com", res.getSubmissionUrl());
    }

    @Test
    void brandCannotSubmit() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        DeliverableSubmissionRequest req = new DeliverableSubmissionRequest();
        req.setSubmissionUrl("http://example.com");
        assertThrows(SecurityException.class, () -> deliverableService.submitDeliverable(100L, 1000L, 1L, req));
    }

    // --- REVIEW ---

    @Test
    void brandCanApproveSubmitted() {
        deliverable.setStatus(DeliverableStatus.SUBMITTED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));
        when(deliverableRepository.save(any(Deliverable.class))).thenAnswer(i -> i.getArgument(0));

        DeliverableReviewRequest req = new DeliverableReviewRequest();
        req.setStatus(DeliverableStatus.APPROVED);

        DeliverableResponse res = deliverableService.reviewDeliverable(100L, 1000L, 1L, req);
        assertEquals(DeliverableStatus.APPROVED, res.getStatus());
    }

    @Test
    void brandCanRejectSubmittedWithFeedback() {
        deliverable.setStatus(DeliverableStatus.SUBMITTED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));
        when(deliverableRepository.save(any(Deliverable.class))).thenAnswer(i -> i.getArgument(0));

        DeliverableReviewRequest req = new DeliverableReviewRequest();
        req.setStatus(DeliverableStatus.REJECTED);
        req.setFeedback("Needs more lighting");

        DeliverableResponse res = deliverableService.reviewDeliverable(100L, 1000L, 1L, req);
        assertEquals(DeliverableStatus.REJECTED, res.getStatus());
        assertEquals("Needs more lighting", res.getFeedback());
    }

    @Test
    void rejectionRequiresFeedback() {
        deliverable.setStatus(DeliverableStatus.SUBMITTED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));

        DeliverableReviewRequest req = new DeliverableReviewRequest();
        req.setStatus(DeliverableStatus.REJECTED);
        req.setFeedback("   "); // blank

        assertThrows(IllegalArgumentException.class, () -> deliverableService.reviewDeliverable(100L, 1000L, 1L, req));
    }

    @Test
    void creatorCannotReview() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        DeliverableReviewRequest req = new DeliverableReviewRequest();
        req.setStatus(DeliverableStatus.APPROVED);
        assertThrows(SecurityException.class, () -> deliverableService.reviewDeliverable(100L, 1000L, 2L, req));
    }

    // --- LISTING ---

    @Test
    void creatorCanList() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findByCollaboration_IdOrderByCreatedAtAsc(100L)).thenReturn(List.of(deliverable));

        List<DeliverableResponse> res = deliverableService.getDeliverables(100L, 2L);
        assertEquals(1, res.size());
    }

    @Test
    void brandCanList() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findByCollaboration_IdOrderByCreatedAtAsc(100L)).thenReturn(List.of(deliverable));

        List<DeliverableResponse> res = deliverableService.getDeliverables(100L, 1L);
        assertEquals(1, res.size());
    }

    @Test
    void creatorCanResubmitRejected() {
        deliverable.setStatus(DeliverableStatus.REJECTED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));
        when(deliverableRepository.save(any(Deliverable.class))).thenAnswer(i -> i.getArgument(0));

        DeliverableSubmissionRequest req = new DeliverableSubmissionRequest();
        req.setSubmissionUrl("http://example.com/resubmit");

        DeliverableResponse res = deliverableService.submitDeliverable(100L, 1000L, 2L, req);
        assertEquals(DeliverableStatus.SUBMITTED, res.getStatus());
        assertEquals("http://example.com/resubmit", res.getSubmissionUrl());
    }

    @Test
    void brandCannotDeleteRejected() {
        deliverable.setStatus(DeliverableStatus.REJECTED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));

        assertThrows(IllegalStateException.class, () -> deliverableService.deleteDeliverable(100L, 1000L, 1L));
        verify(deliverableRepository, never()).delete(any(Deliverable.class));
    }

    @Test
    void idMismatchRejectsMutation() {
        Collaboration diffCollab = new Collaboration();
        diffCollab.setId(999L);
        deliverable.setCollaboration(diffCollab);

        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));

        DeliverableUpdateRequest req = new DeliverableUpdateRequest();
        assertThrows(IllegalArgumentException.class, () -> deliverableService.updateDeliverable(100L, 1000L, 1L, req));
        verify(deliverableRepository, never()).save(any(Deliverable.class));
    }

    @Test
    void approvedIsImmutable() {
        deliverable.setStatus(DeliverableStatus.APPROVED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));

        // Attempt Edit
        DeliverableUpdateRequest editReq = new DeliverableUpdateRequest();
        assertThrows(IllegalStateException.class, () -> deliverableService.updateDeliverable(100L, 1000L, 1L, editReq));

        // Attempt Delete
        assertThrows(IllegalStateException.class, () -> deliverableService.deleteDeliverable(100L, 1000L, 1L));

        // Attempt Review
        DeliverableReviewRequest revReq = new DeliverableReviewRequest();
        assertThrows(IllegalStateException.class, () -> deliverableService.reviewDeliverable(100L, 1000L, 1L, revReq));
        
        // Attempt Submit (as Creator)
        DeliverableSubmissionRequest subReq = new DeliverableSubmissionRequest();
        assertThrows(IllegalStateException.class, () -> deliverableService.submitDeliverable(100L, 1000L, 2L, subReq));
    }

    @Test
    void dualContextUserAuthorization() {
        User dualContextUser = new User();
        dualContextUser.setId(55L);
        campaign.setBrandUser(dualContextUser);
        
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        
        DeliverableCreateRequest createReq = new DeliverableCreateRequest();
        createReq.setTitle("Brand Create");
        
        when(deliverableRepository.save(any(Deliverable.class))).thenAnswer(i -> i.getArgument(0));
        
        // Should succeed because user 55 is the Brand user for THIS collaboration
        DeliverableResponse res = deliverableService.createDeliverable(100L, 55L, createReq);
        assertEquals("Brand Create", res.getTitle());

        // Should fail for submission because user 55 is NOT the creator user
        DeliverableSubmissionRequest subReq = new DeliverableSubmissionRequest();
        assertThrows(SecurityException.class, () -> deliverableService.submitDeliverable(100L, 1000L, 55L, subReq));
    }

    @Test
    void rejectionRequiresNonBlankFeedback() {
        deliverable.setStatus(DeliverableStatus.SUBMITTED);
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        when(deliverableRepository.findById(1000L)).thenReturn(Optional.of(deliverable));

        DeliverableReviewRequest req = new DeliverableReviewRequest();
        req.setStatus(DeliverableStatus.REJECTED);
        req.setFeedback(null); 

        assertThrows(IllegalArgumentException.class, () -> deliverableService.reviewDeliverable(100L, 1000L, 1L, req));
    }

    @Test
    void unrelatedCannotList() {
        when(collaborationRepository.findById(100L)).thenReturn(Optional.of(collaboration));
        assertThrows(SecurityException.class, () -> deliverableService.getDeliverables(100L, 3L));
    }
}
