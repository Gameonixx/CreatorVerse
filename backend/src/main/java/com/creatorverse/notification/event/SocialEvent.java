package com.creatorverse.notification.event;

import com.creatorverse.notification.entity.enums.ReferenceType;

public record SocialEvent(
    Long recipientUserId,
    String actorName,
    String action, // "followed you", "liked your post", "commented on your post"
    ReferenceType referenceType,
    Long referenceId
) {}
