package com.creatorverse.notification.event;

import com.creatorverse.notification.entity.enums.ReferenceType;

public record ApplicationStatusChangedEvent(
    Long recipientUserId,
    String campaignTitle,
    boolean accepted,
    ReferenceType referenceType,
    Long referenceId
) {}
