package com.creatorverse.social.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.UnauthorizedException;
import com.creatorverse.social.dto.FollowResponse;
import com.creatorverse.social.service.FollowService;
import com.creatorverse.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/follow/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FollowResponse> followUser(@PathVariable Long userId) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        FollowResponse response = followService.followUser(currentUsername, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/unfollow/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unfollowUser(@PathVariable Long userId) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        followService.unfollowUser(currentUsername, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable Long userId) {
        List<UserResponse> responses = followService.getFollowers(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<UserResponse>> getFollowing(@PathVariable Long userId) {
        List<UserResponse> responses = followService.getFollowing(userId);
        return ResponseEntity.ok(responses);
    }
}
