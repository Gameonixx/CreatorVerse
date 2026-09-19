package com.creatorverse.collaboration.dto;

import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CollaborationResponse {
    
    private Long id;
    private Long campaignId;
    private String campaignTitle;
    private String campaignDescription;
    private String campaignNiche;
    private BigDecimal campaignBudget;
    
    private Long originatingApplicationId;
    private CollaborationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Long creatorUserId;
    private String creatorName;
    private String creatorAvatarUrl;
    
    private Long brandUserId;
    private String brandName;
    private String brandLogoUrl;

    public CollaborationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }

    public String getCampaignTitle() { return campaignTitle; }
    public void setCampaignTitle(String campaignTitle) { this.campaignTitle = campaignTitle; }

    public String getCampaignDescription() { return campaignDescription; }
    public void setCampaignDescription(String campaignDescription) { this.campaignDescription = campaignDescription; }

    public String getCampaignNiche() { return campaignNiche; }
    public void setCampaignNiche(String campaignNiche) { this.campaignNiche = campaignNiche; }

    public BigDecimal getCampaignBudget() { return campaignBudget; }
    public void setCampaignBudget(BigDecimal campaignBudget) { this.campaignBudget = campaignBudget; }

    public Long getOriginatingApplicationId() { return originatingApplicationId; }
    public void setOriginatingApplicationId(Long originatingApplicationId) { this.originatingApplicationId = originatingApplicationId; }

    public CollaborationStatus getStatus() { return status; }
    public void setStatus(CollaborationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long creatorUserId) { this.creatorUserId = creatorUserId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorAvatarUrl() { return creatorAvatarUrl; }
    public void setCreatorAvatarUrl(String creatorAvatarUrl) { this.creatorAvatarUrl = creatorAvatarUrl; }

    public Long getBrandUserId() { return brandUserId; }
    public void setBrandUserId(Long brandUserId) { this.brandUserId = brandUserId; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getBrandLogoUrl() { return brandLogoUrl; }
    public void setBrandLogoUrl(String brandLogoUrl) { this.brandLogoUrl = brandLogoUrl; }
}
