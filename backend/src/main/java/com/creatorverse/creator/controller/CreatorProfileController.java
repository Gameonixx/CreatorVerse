package com.creatorverse.creator.controller;

import com.creatorverse.creator.dto.CreatorProfileCreateRequest;
import com.creatorverse.creator.dto.CreatorProfileResponse;
import com.creatorverse.creator.dto.CreatorProfileUpdateRequest;
import com.creatorverse.creator.service.CreatorProfileService;
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
@RequestMapping("/api/creators/profile")
public class CreatorProfileController {

    private final CreatorProfileService creatorProfileService;
    private final UserService userService;

    public CreatorProfileController(CreatorProfileService creatorProfileService, UserService userService) {
        this.creatorProfileService = creatorProfileService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreatorProfileResponse> createProfile(@RequestParam Long userId, @Valid @RequestBody CreatorProfileCreateRequest request) {
        verifyOwnershipOrAdmin(userId);
        CreatorProfileResponse response = creatorProfileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CreatorProfileResponse> getProfile(@PathVariable Long userId) {
        verifyOwnershipOrAdmin(userId);
        CreatorProfileResponse response = creatorProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/{userId}")
    public ResponseEntity<CreatorProfileResponse> getPublicProfile(@PathVariable Long userId) {
        // No ownership or admin verification - explicitly public
        CreatorProfileResponse response = creatorProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreatorProfileResponse> updateProfile(@PathVariable Long userId, @Valid @RequestBody CreatorProfileUpdateRequest request) {
        verifyOwnershipOrAdmin(userId);
        CreatorProfileResponse response = creatorProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long userId) {
        verifyOwnershipOrAdmin(userId);
        creatorProfileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
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
