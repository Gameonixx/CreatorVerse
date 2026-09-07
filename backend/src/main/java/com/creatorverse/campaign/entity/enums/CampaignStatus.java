package com.creatorverse.campaign.entity.enums;

public enum CampaignStatus {
    DRAFT,
    OPEN,
    PAUSED,
    CLOSED,
    COMPLETED;

    public boolean canTransitionTo(CampaignStatus newStatus) {
        if (this == newStatus) return true;
        return switch (this) {
            case DRAFT -> newStatus == OPEN;
            case OPEN -> newStatus == PAUSED || newStatus == CLOSED;
            case PAUSED -> newStatus == OPEN || newStatus == CLOSED;
            case CLOSED -> newStatus == COMPLETED;
            case COMPLETED -> false; // Terminal state
        };
    }
}
