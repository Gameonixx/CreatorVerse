package com.creatorverse.campaign.application.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.campaign.application.dto.ApplicationCreateRequest;
import com.creatorverse.campaign.application.dto.ApplicationResponse;
import com.creatorverse.campaign.application.dto.ApplicationReviewRequest;
import com.creatorverse.campaign.application.service.CampaignApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.user.service.UserService;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignApplicationController {

    private final CampaignApplicationService applicationService;
    private final UserService userService;

    public CampaignApplicationController(CampaignApplicationService applicationService, UserService userService) {
        this.applicationService = applicationService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new ForbiddenException("Not authenticated");
        return userService.getUserByUsername(username).getId();
    }

    @PostMapping("/{id}/applications")
    public ResponseEntity<ApplicationResponse> applyToCampaign(
            @PathVariable Long id,
            @RequestBody ApplicationCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(applicationService.applyToCampaign(currentUserId, id, request));
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<Page<ApplicationResponse>> getCampaignApplications(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Long currentUserId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(applicationService.getCampaignApplications(currentUserId, id, pageable));
    }

    @GetMapping("/applications/mine")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Long currentUserId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(applicationService.getMyApplications(currentUserId, pageable));
    }

    @PutMapping("/{campaignId}/applications/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> reviewApplication(
            @PathVariable Long campaignId,
            @PathVariable Long applicationId,
            @RequestBody ApplicationReviewRequest request) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(applicationService.reviewApplication(currentUserId, campaignId, applicationId, request));
    }

    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long applicationId) {
        Long currentUserId = getCurrentUserId();
        applicationService.withdrawApplication(currentUserId, applicationId);
        return ResponseEntity.noContent().build();
    }
}
