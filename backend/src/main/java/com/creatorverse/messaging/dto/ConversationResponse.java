package com.creatorverse.messaging.dto;

import java.time.LocalDateTime;

public class ConversationResponse {
    private Long id;
    private Long collaborationId;
    private long unreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ConversationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCollaborationId() { return collaborationId; }
    public void setCollaborationId(Long collaborationId) { this.collaborationId = collaborationId; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
