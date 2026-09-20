package com.creatorverse.collaboration.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.collaboration.dto.CollaborationResponse;
import com.creatorverse.collaboration.dto.CollaborationStatusUpdateRequest;
import com.creatorverse.collaboration.service.CollaborationService;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collaborations")
public class CollaborationController {

    private final CollaborationService collaborationService;
    private final UserRepository userRepository;

    public CollaborationController(CollaborationService collaborationService, UserRepository userRepository) {
        this.collaborationService = collaborationService;
        this.userRepository = userRepository;
    }

    private User getCurrentAuthenticatedUser() {
        String username = SecurityUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new SecurityException("Authenticated user not found."));
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CollaborationResponse>> getMyCollaborations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        User currentUser = getCurrentAuthenticatedUser();
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CollaborationResponse> collaborations = collaborationService.getMyCollaborations(currentUser.getId(), pageable);
        return ResponseEntity.ok(collaborations);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollaborationResponse> getCollaboration(@PathVariable Long id) {
        User currentUser = getCurrentAuthenticatedUser();
        CollaborationResponse collaboration = collaborationService.getCollaboration(id, currentUser.getId());
        return ResponseEntity.ok(collaboration);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollaborationResponse> updateCollaborationStatus(
            @PathVariable Long id,
            @Valid @RequestBody CollaborationStatusUpdateRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        CollaborationResponse updated = collaborationService.updateStatus(id, currentUser.getId(), request.getStatus());
        return ResponseEntity.ok(updated);
    }
}
