package com.creatorverse.campaign.application.dto;

import com.creatorverse.campaign.application.entity.enums.ApplicationStatus;

import java.time.LocalDateTime;

public class ApplicationResponse {
    private Long id;
    private Long campaignId;
    private Long creatorUserId;
    private String creatorName;
    private String creatorAvatarUrl;
    private Integer creatorFollowerCount;
    private Double creatorEngagementRate;
    private String message;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApplicationResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }

    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long creatorUserId) { this.creatorUserId = creatorUserId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorAvatarUrl() { return creatorAvatarUrl; }
    public void setCreatorAvatarUrl(String creatorAvatarUrl) { this.creatorAvatarUrl = creatorAvatarUrl; }

    public Integer getCreatorFollowerCount() { return creatorFollowerCount; }
    public void setCreatorFollowerCount(Integer creatorFollowerCount) { this.creatorFollowerCount = creatorFollowerCount; }

    public Double getCreatorEngagementRate() { return creatorEngagementRate; }
    public void setCreatorEngagementRate(Double creatorEngagementRate) { this.creatorEngagementRate = creatorEngagementRate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
