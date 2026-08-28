package com.creatorverse.social.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.UnauthorizedException;
import com.creatorverse.social.dto.LikeResponse;
import com.creatorverse.social.service.LikeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/content/{contentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LikeResponse> likeContent(@PathVariable Long contentId) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        LikeResponse response = likeService.likeContent(currentUsername, contentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/content/{contentId}/unlike")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikeContent(@PathVariable Long contentId) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        likeService.unlikeContent(currentUsername, contentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/content/{contentId}/likes")
    public ResponseEntity<List<LikeResponse>> getLikes(@PathVariable Long contentId) {
        List<LikeResponse> responses = likeService.getLikes(contentId);
        return ResponseEntity.ok(responses);
    }
}
