package com.creatorverse.content.controller;

import com.creatorverse.content.dto.ContentCreateRequest;
import com.creatorverse.content.dto.ContentResponse;
import com.creatorverse.content.dto.ContentSummaryResponse;
import com.creatorverse.content.dto.ContentUpdateRequest;
import com.creatorverse.content.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContentResponse> createContent(
            @RequestPart("metadata") @Valid ContentCreateRequest request,
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean publishNow) {
        
        ContentResponse response = contentService.createContent(request, file, publishNow);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentResponse> getContent(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.getContent(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContentResponse> updateContent(
            @PathVariable Long id, 
            @Valid @RequestBody ContentUpdateRequest request) {
        return ResponseEntity.ok(contentService.updateContent(id, request));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContentResponse> publishContent(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.publishContent(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<org.springframework.data.domain.Page<ContentResponse>> getPublicFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(contentService.getPublicFeed(page, size));
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<org.springframework.data.domain.Page<ContentResponse>> getPublicContentByCreator(
            @PathVariable Long creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(contentService.getPublicContentByCreatorId(creatorId, page, size));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContentSummaryResponse>> getMyContent() {
        return ResponseEntity.ok(contentService.getMyContent());
    }

    @GetMapping("/me/drafts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContentSummaryResponse>> getMyDrafts() {
        return ResponseEntity.ok(contentService.getMyDrafts());
    }

    @GetMapping("/me/published")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContentSummaryResponse>> getMyPublished() {
        return ResponseEntity.ok(contentService.getMyPublished());
    }
}
