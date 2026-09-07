package com.creatorverse.campaign.application.dto;

import com.creatorverse.campaign.application.entity.enums.ApplicationStatus;

public class ApplicationReviewRequest {
    private ApplicationStatus status;

    public ApplicationReviewRequest() {}

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
}
