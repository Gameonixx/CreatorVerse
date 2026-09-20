package com.creatorverse.collaboration.deliverable.dto;

import com.creatorverse.collaboration.deliverable.entity.enums.DeliverableStatus;
import jakarta.validation.constraints.NotNull;

public class DeliverableReviewRequest {

    @NotNull(message = "Status is required")
    private DeliverableStatus status;

    private String feedback;

    public DeliverableReviewRequest() {}

    public DeliverableStatus getStatus() { return status; }
    public void setStatus(DeliverableStatus status) { this.status = status; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
