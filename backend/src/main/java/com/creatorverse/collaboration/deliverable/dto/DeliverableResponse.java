package com.creatorverse.collaboration.deliverable.dto;

import com.creatorverse.collaboration.deliverable.entity.enums.DeliverableStatus;

import java.time.LocalDateTime;

public class DeliverableResponse {

    private Long id;
    private Long collaborationId;
    private String title;
    private String description;
    private String submissionUrl;
    private DeliverableStatus status;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DeliverableResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCollaborationId() { return collaborationId; }
    public void setCollaborationId(Long collaborationId) { this.collaborationId = collaborationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSubmissionUrl() { return submissionUrl; }
    public void setSubmissionUrl(String submissionUrl) { this.submissionUrl = submissionUrl; }

    public DeliverableStatus getStatus() { return status; }
    public void setStatus(DeliverableStatus status) { this.status = status; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
