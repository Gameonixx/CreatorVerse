package com.creatorverse.user.controller;

import com.creatorverse.user.dto.UserProfileCreateRequest;
import com.creatorverse.user.dto.UserProfileResponse;
import com.creatorverse.user.dto.UserProfileUpdateRequest;
import com.creatorverse.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.user.service.UserService;
import com.creatorverse.user.dto.UserResponse;

@RestController
@RequestMapping("/api/users/{userId}/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserService userService;

    public UserProfileController(UserProfileService userProfileService, UserService userService) {
        this.userProfileService = userProfileService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> createProfile(@PathVariable Long userId, @Valid @RequestBody UserProfileCreateRequest request) {
        verifyOwnershipOrAdmin(userId);
        UserProfileResponse response = userProfileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId) {
        verifyOwnershipOrAdmin(userId);
        UserProfileResponse response = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(@PathVariable Long userId, @Valid @RequestBody UserProfileUpdateRequest request) {
        verifyOwnershipOrAdmin(userId);
        UserProfileResponse response = userProfileService.updateProfile(userId, request);
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
