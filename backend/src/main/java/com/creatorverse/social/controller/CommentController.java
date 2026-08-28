package com.creatorverse.social.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.UnauthorizedException;
import com.creatorverse.social.dto.CommentRequest;
import com.creatorverse.social.dto.CommentResponse;
import com.creatorverse.social.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/content/{contentId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long contentId,
            @Valid @RequestBody CommentRequest request) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        CommentResponse response = commentService.createComment(currentUsername, contentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/content/{contentId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long contentId) {
        List<CommentResponse> responses = commentService.getComments(contentId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        commentService.deleteComment(currentUsername, commentId);
        return ResponseEntity.noContent().build();
    }
}
