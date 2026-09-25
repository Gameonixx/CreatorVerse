package com.creatorverse.notification.controller;

import com.creatorverse.auth.security.SecurityUtils;
import com.creatorverse.notification.dto.NotificationResponse;
import com.creatorverse.notification.dto.NotificationUnreadCountResponse;
import com.creatorverse.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(notificationService.getNotifications(username, PageRequest.of(page, size)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<NotificationUnreadCountResponse> getUnreadCount() {
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(notificationService.getUnreadCount(username));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        String username = SecurityUtils.getCurrentUsername();
        notificationService.markAsRead(username, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        String username = SecurityUtils.getCurrentUsername();
        notificationService.markAllAsRead(username);
        return ResponseEntity.ok().build();
    }
}
