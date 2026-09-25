package com.creatorverse.notification.service;

import com.creatorverse.notification.entity.enums.NotificationType;
import com.creatorverse.notification.event.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationEventListener {
    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMessageCreatedEvent(MessageCreatedEvent event) {
        notificationService.createNotification(
            event.recipientUserId(),
            NotificationType.MESSAGE,
            "New message from " + event.senderName(),
            event.senderName() + " sent you a message.",
            com.creatorverse.notification.entity.enums.ReferenceType.COLLABORATION,
            event.collaborationId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleApplicationStatusChangedEvent(ApplicationStatusChangedEvent event) {
        String action = event.accepted() ? "accepted" : "rejected";
        notificationService.createNotification(
            event.recipientUserId(),
            NotificationType.APPLICATION_UPDATE,
            "Application " + action,
            "Your application for \"" + event.campaignTitle() + "\" was " + action + ".",
            event.referenceType(),
            event.referenceId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleDeliverableStatusChangedEvent(DeliverableStatusChangedEvent event) {
        String title = "Deliverable " + event.action();
        String message = event.actorName() + " " + event.action() + " the deliverable \"" + event.deliverableTitle() + "\".";
        notificationService.createNotification(
            event.recipientUserId(),
            NotificationType.DELIVERABLE_UPDATE,
            title,
            message,
            com.creatorverse.notification.entity.enums.ReferenceType.COLLABORATION,
            event.collaborationId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSocialEvent(SocialEvent event) {
        notificationService.createNotification(
            event.recipientUserId(),
            NotificationType.SOCIAL,
            "New social interaction",
            event.actorName() + " " + event.action() + ".",
            event.referenceType(),
            event.referenceId()
        );
    }
}
