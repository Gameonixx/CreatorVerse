package com.creatorverse.messaging.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.messaging.dto.ConversationResponse;
import com.creatorverse.messaging.dto.MessageResponse;
import com.creatorverse.messaging.dto.MessageSendRequest;
import com.creatorverse.messaging.dto.UnreadCountResponse;
import com.creatorverse.messaging.service.MessagingService;
import com.creatorverse.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collaborations/{id}")
public class MessagingController {

    private final MessagingService messagingService;
    private final UserService userService;

    public MessagingController(MessagingService messagingService, UserService userService) {
        this.messagingService = messagingService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new ForbiddenException("Not authenticated");
        return userService.getUserByUsername(username).getId();
    }

    @GetMapping("/conversation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> getConversation(@PathVariable Long id) {
        return ResponseEntity.ok(messagingService.getConversation(id, getCurrentUserId()));
    }

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        // Sorting chronologically (oldest first or newest first) - usually newest first for pagination, then reversed on frontend.
        // Let's use newest first (desc) for pagination.
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(messagingService.getMessages(id, getCurrentUserId(), pageable));
    }

    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody MessageSendRequest request) {
        return ResponseEntity.ok(messagingService.sendMessage(id, getCurrentUserId(), request.getContent()));
    }

    @PutMapping("/messages/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnreadCountResponse> markMessagesAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(messagingService.markAsRead(id, getCurrentUserId()));
    }
}
