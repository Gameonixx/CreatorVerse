package com.creatorverse.discovery.dto;

public class CreatorCardResponse {
    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Integer followerCount;
    private String niche;
    private Double engagementRate; // Stored metadata, not actively calculated

    public CreatorCardResponse() {}

    public CreatorCardResponse(Long userId, String username, String displayName, String avatarUrl, 
                               String bio, Integer followerCount, String niche, Double engagementRate) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.followerCount = followerCount;
        this.niche = niche;
        this.engagementRate = engagementRate;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public Integer getFollowerCount() { return followerCount; }
    public void setFollowerCount(Integer followerCount) { this.followerCount = followerCount; }
    
    public String getNiche() { return niche; }
    public void setNiche(String niche) { this.niche = niche; }
    
    public Double getEngagementRate() { return engagementRate; }
    public void setEngagementRate(Double engagementRate) { this.engagementRate = engagementRate; }
}
