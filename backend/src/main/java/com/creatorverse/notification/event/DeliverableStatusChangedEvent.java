package com.creatorverse.notification.event;

public record DeliverableStatusChangedEvent(
    Long recipientUserId,
    String actorName,
    String deliverableTitle,
    String action, // "submitted", "approved", "rejected"
    Long collaborationId
) {}
