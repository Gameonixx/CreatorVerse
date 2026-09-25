package com.creatorverse.notification.event;

public record MessageCreatedEvent(
    Long recipientUserId,
    String senderName,
    Long collaborationId
) {}
