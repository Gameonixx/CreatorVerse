package com.creatorverse.campaign.dto;

import com.creatorverse.campaign.entity.enums.CampaignStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CampaignUpdateRequest {
    private String title;
    private String description;
    private String niche;
    private BigDecimal budget;
    private CampaignStatus status;
    private LocalDateTime applicationDeadline;

    public CampaignUpdateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNiche() { return niche; }
    public void setNiche(String niche) { this.niche = niche; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }

    public LocalDateTime getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDateTime applicationDeadline) { this.applicationDeadline = applicationDeadline; }
}
