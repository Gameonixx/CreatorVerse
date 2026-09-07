package com.creatorverse.campaign.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.campaign.dto.CampaignCreateRequest;
import com.creatorverse.campaign.dto.CampaignResponse;
import com.creatorverse.campaign.dto.CampaignUpdateRequest;
import com.creatorverse.campaign.service.CampaignService;
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
public class CampaignController {

    private final CampaignService campaignService;
    private final UserService userService;

    public CampaignController(CampaignService campaignService, UserService userService) {
        this.campaignService = campaignService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new ForbiddenException("Not authenticated");
        return userService.getUserByUsername(username).getId();
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(@RequestBody CampaignCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(campaignService.createCampaign(currentUserId, request));
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<CampaignResponse>> getMyCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Long currentUserId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(campaignService.getMyCampaigns(currentUserId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaign(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateCampaign(@PathVariable Long id, @RequestBody CampaignUpdateRequest request) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(campaignService.updateCampaign(currentUserId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        campaignService.deleteCampaign(currentUserId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<CampaignResponse>> discoverCampaigns(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String niche,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(campaignService.discoverCampaigns(search, niche, pageable));
    }
}
