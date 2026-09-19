package com.creatorverse.collaboration.dto;

import com.creatorverse.collaboration.entity.enums.CollaborationStatus;
import jakarta.validation.constraints.NotNull;

public class CollaborationStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private CollaborationStatus status;

    public CollaborationStatusUpdateRequest() {}

    public CollaborationStatus getStatus() { return status; }
    public void setStatus(CollaborationStatus status) { this.status = status; }
}
