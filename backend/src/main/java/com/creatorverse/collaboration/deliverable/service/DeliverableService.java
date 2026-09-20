package com.creatorverse.collaboration.deliverable.service;

import com.creatorverse.collaboration.deliverable.dto.*;
import com.creatorverse.collaboration.deliverable.entity.Deliverable;
import com.creatorverse.collaboration.deliverable.entity.enums.DeliverableStatus;
import com.creatorverse.collaboration.deliverable.repository.DeliverableRepository;
import com.creatorverse.collaboration.entity.Collaboration;
import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import com.creatorverse.collaboration.repository.CollaborationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final CollaborationRepository collaborationRepository;

    public DeliverableService(DeliverableRepository deliverableRepository, CollaborationRepository collaborationRepository) {
        this.deliverableRepository = deliverableRepository;
        this.collaborationRepository = collaborationRepository;
    }

    private Collaboration getCollaborationAndValidateBrand(Long collaborationId, Long userId) {
        Collaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new IllegalArgumentException("Collaboration not found."));
        if (!collaboration.getCampaign().getBrandUser().getId().equals(userId)) {
            throw new SecurityException("User is not the brand owner of this collaboration.");
        }
        return collaboration;
    }

    private Collaboration getCollaborationAndValidateCreator(Long collaborationId, Long userId) {
        Collaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new IllegalArgumentException("Collaboration not found."));
        if (!collaboration.getCreatorUser().getId().equals(userId)) {
            throw new SecurityException("User is not the creator of this collaboration.");
        }
        return collaboration;
    }

    private Collaboration getCollaborationAndValidateAccess(Long collaborationId, Long userId) {
        Collaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new IllegalArgumentException("Collaboration not found."));
        if (!collaboration.getCreatorUser().getId().equals(userId) && !collaboration.getCampaign().getBrandUser().getId().equals(userId)) {
            throw new SecurityException("User is not authorized to access this collaboration.");
        }
        return collaboration;
    }

    private void ensureActive(Collaboration collaboration) {
        if (collaboration.getStatus() != CollaborationStatus.ACTIVE) {
            throw new IllegalStateException("Deliverable mutation is allowed ONLY while Collaboration is ACTIVE.");
        }
    }

    @Transactional
    public DeliverableResponse createDeliverable(Long collaborationId, Long userId, DeliverableCreateRequest request) {
        Collaboration collaboration = getCollaborationAndValidateBrand(collaborationId, userId);
        ensureActive(collaboration);

        Deliverable deliverable = new Deliverable();
        deliverable.setCollaboration(collaboration);
        deliverable.setTitle(request.getTitle());
        deliverable.setDescription(request.getDescription());
        deliverable.setStatus(DeliverableStatus.PENDING);

        return mapToResponse(deliverableRepository.save(deliverable));
    }

    @Transactional
    public DeliverableResponse updateDeliverable(Long collaborationId, Long deliverableId, Long userId, DeliverableUpdateRequest request) {
        Collaboration collaboration = getCollaborationAndValidateBrand(collaborationId, userId);
        ensureActive(collaboration);

        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new IllegalArgumentException("Deliverable not found."));
        
        if (!deliverable.getCollaboration().getId().equals(collaborationId)) {
            throw new IllegalArgumentException("Deliverable does not belong to this collaboration.");
        }

        if (deliverable.getStatus() != DeliverableStatus.PENDING && deliverable.getStatus() != DeliverableStatus.REJECTED) {
            throw new IllegalStateException("Only PENDING or REJECTED deliverables can be edited.");
        }

        deliverable.setTitle(request.getTitle());
        if (request.getDescription() != null) {
            deliverable.setDescription(request.getDescription());
        }

        return mapToResponse(deliverableRepository.save(deliverable));
    }

    @Transactional
    public void deleteDeliverable(Long collaborationId, Long deliverableId, Long userId) {
        Collaboration collaboration = getCollaborationAndValidateBrand(collaborationId, userId);
        ensureActive(collaboration);

        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new IllegalArgumentException("Deliverable not found."));

        if (!deliverable.getCollaboration().getId().equals(collaborationId)) {
            throw new IllegalArgumentException("Deliverable does not belong to this collaboration.");
        }

        if (deliverable.getStatus() != DeliverableStatus.PENDING) {
            throw new IllegalStateException("Only PENDING deliverables can be deleted.");
        }

        deliverableRepository.delete(deliverable);
    }

    @Transactional
    public DeliverableResponse submitDeliverable(Long collaborationId, Long deliverableId, Long userId, DeliverableSubmissionRequest request) {
        Collaboration collaboration = getCollaborationAndValidateCreator(collaborationId, userId);
        ensureActive(collaboration);

        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new IllegalArgumentException("Deliverable not found."));

        if (!deliverable.getCollaboration().getId().equals(collaborationId)) {
            throw new IllegalArgumentException("Deliverable does not belong to this collaboration.");
        }

        if (deliverable.getStatus() != DeliverableStatus.PENDING && deliverable.getStatus() != DeliverableStatus.REJECTED) {
            throw new IllegalStateException("Only PENDING or REJECTED deliverables can be submitted.");
        }

        deliverable.setSubmissionUrl(request.getSubmissionUrl());
        deliverable.setStatus(DeliverableStatus.SUBMITTED);

        return mapToResponse(deliverableRepository.save(deliverable));
    }

    @Transactional
    public DeliverableResponse reviewDeliverable(Long collaborationId, Long deliverableId, Long userId, DeliverableReviewRequest request) {
        Collaboration collaboration = getCollaborationAndValidateBrand(collaborationId, userId);
        ensureActive(collaboration);

        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new IllegalArgumentException("Deliverable not found."));

        if (!deliverable.getCollaboration().getId().equals(collaborationId)) {
            throw new IllegalArgumentException("Deliverable does not belong to this collaboration.");
        }

        if (deliverable.getStatus() != DeliverableStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED deliverables can be reviewed.");
        }

        if (request.getStatus() != DeliverableStatus.APPROVED && request.getStatus() != DeliverableStatus.REJECTED) {
            throw new IllegalArgumentException("Review status must be APPROVED or REJECTED.");
        }

        if (request.getStatus() == DeliverableStatus.REJECTED) {
            if (request.getFeedback() == null || request.getFeedback().trim().isEmpty()) {
                throw new IllegalArgumentException("Feedback is mandatory when rejecting a deliverable.");
            }
            deliverable.setFeedback(request.getFeedback());
        }

        deliverable.setStatus(request.getStatus());

        return mapToResponse(deliverableRepository.save(deliverable));
    }

    @Transactional(readOnly = true)
    public List<DeliverableResponse> getDeliverables(Long collaborationId, Long userId) {
        getCollaborationAndValidateAccess(collaborationId, userId);
        List<Deliverable> deliverables = deliverableRepository.findByCollaboration_IdOrderByCreatedAtAsc(collaborationId);
        return deliverables.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private DeliverableResponse mapToResponse(Deliverable deliverable) {
        DeliverableResponse response = new DeliverableResponse();
        response.setId(deliverable.getId());
        response.setCollaborationId(deliverable.getCollaboration().getId());
        response.setTitle(deliverable.getTitle());
        response.setDescription(deliverable.getDescription());
        response.setSubmissionUrl(deliverable.getSubmissionUrl());
        response.setStatus(deliverable.getStatus());
        response.setFeedback(deliverable.getFeedback());
        response.setCreatedAt(deliverable.getCreatedAt());
        response.setUpdatedAt(deliverable.getUpdatedAt());
        return response;
    }
}
