package com.creatorverse.creator.dto;

import java.time.LocalDateTime;

public class CreatorProfileResponse {
    private Long id;
    private Long userId;
    private String niche;
    private Double engagementRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Boolean isFollowedByCurrentUser;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNiche() { return niche; }
    public void setNiche(String niche) { this.niche = niche; }


    public Double getEngagementRate() { return engagementRate; }
    public void setEngagementRate(Double engagementRate) { this.engagementRate = engagementRate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsFollowedByCurrentUser() { return isFollowedByCurrentUser; }
    public void setIsFollowedByCurrentUser(Boolean followedByCurrentUser) { isFollowedByCurrentUser = followedByCurrentUser; }
}
