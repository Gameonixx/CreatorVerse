package com.creatorverse.brand.controller;

import com.creatorverse.brand.dto.BrandProfileCreateRequest;
import com.creatorverse.brand.dto.BrandProfileResponse;
import com.creatorverse.brand.dto.BrandProfileUpdateRequest;
import com.creatorverse.brand.service.BrandProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.user.service.UserService;
import com.creatorverse.user.dto.UserResponse;

@RestController
@RequestMapping("/api/brands/profile")
public class BrandProfileController {

    private final BrandProfileService brandProfileService;
    private final UserService userService;

    public BrandProfileController(BrandProfileService brandProfileService, UserService userService) {
        this.brandProfileService = brandProfileService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('BRAND') or hasRole('ADMIN')")
    public ResponseEntity<BrandProfileResponse> createProfile(@RequestParam Long userId, @Valid @RequestBody BrandProfileCreateRequest request) {
        verifyOwnershipOrAdmin(userId);
        BrandProfileResponse response = brandProfileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BrandProfileResponse> getProfile(@PathVariable Long userId) {
        verifyOwnershipOrAdmin(userId);
        BrandProfileResponse response = brandProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('BRAND') or hasRole('ADMIN')")
    public ResponseEntity<BrandProfileResponse> updateProfile(@PathVariable Long userId, @Valid @RequestBody BrandProfileUpdateRequest request) {
        verifyOwnershipOrAdmin(userId);
        BrandProfileResponse response = brandProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    private void verifyOwnershipOrAdmin(Long id) {
        if (SecurityUtils.hasRole("ADMIN")) return;
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new ForbiddenException("Not authenticated");
        }
        UserResponse user = userService.getUser(id);
        if (!user.getUsername().equals(currentUsername)) {
            throw new ForbiddenException("You do not have permission to access this resource");
        }
    }
}
