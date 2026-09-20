package com.creatorverse.collaboration.deliverable.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.collaboration.deliverable.dto.*;
import com.creatorverse.collaboration.deliverable.service.DeliverableService;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaborations/{id}/deliverables")
public class DeliverableController {

    private final DeliverableService deliverableService;
    private final UserRepository userRepository;

    public DeliverableController(DeliverableService deliverableService, UserRepository userRepository) {
        this.deliverableService = deliverableService;
        this.userRepository = userRepository;
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new SecurityException("Authenticated user not found."));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliverableResponse> createDeliverable(
            @PathVariable Long id,
            @Valid @RequestBody DeliverableCreateRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        DeliverableResponse response = deliverableService.createDeliverable(id, currentUser.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DeliverableResponse>> getDeliverables(@PathVariable Long id) {
        User currentUser = getCurrentAuthenticatedUser();
        List<DeliverableResponse> responses = deliverableService.getDeliverables(id, currentUser.getId());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{deliverableId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliverableResponse> updateDeliverable(
            @PathVariable Long id,
            @PathVariable Long deliverableId,
            @Valid @RequestBody DeliverableUpdateRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        DeliverableResponse response = deliverableService.updateDeliverable(id, deliverableId, currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{deliverableId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteDeliverable(
            @PathVariable Long id,
            @PathVariable Long deliverableId) {
        User currentUser = getCurrentAuthenticatedUser();
        deliverableService.deleteDeliverable(id, deliverableId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{deliverableId}/submission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliverableResponse> submitDeliverable(
            @PathVariable Long id,
            @PathVariable Long deliverableId,
            @Valid @RequestBody DeliverableSubmissionRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        DeliverableResponse response = deliverableService.submitDeliverable(id, deliverableId, currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{deliverableId}/review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliverableResponse> reviewDeliverable(
            @PathVariable Long id,
            @PathVariable Long deliverableId,
            @Valid @RequestBody DeliverableReviewRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        DeliverableResponse response = deliverableService.reviewDeliverable(id, deliverableId, currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }
}
