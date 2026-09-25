package com.creatorverse.notification.service;

import com.creatorverse.common.exception.ResourceNotFoundException;
import com.creatorverse.common.exception.ForbiddenException;
import com.creatorverse.notification.dto.NotificationResponse;
import com.creatorverse.notification.dto.NotificationUnreadCountResponse;
import com.creatorverse.notification.entity.Notification;
import com.creatorverse.notification.entity.enums.NotificationType;
import com.creatorverse.notification.entity.enums.ReferenceType;
import com.creatorverse.notification.repository.NotificationRepository;
import com.creatorverse.user.entity.User;
import com.creatorverse.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.findByRecipientUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public NotificationUnreadCountResponse getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        long count = notificationRepository.countByRecipientUserAndIsReadFalse(user);
        return new NotificationUnreadCountResponse(count);
    }

    @Transactional
    public void markAsRead(String username, Long notificationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getRecipientUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Cannot mark another user's notification as read");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        notificationRepository.markAllAsReadByRecipientUser(user);
    }

    @Transactional
    public void createNotification(Long recipientUserId, NotificationType type, String title, String message, ReferenceType referenceType, Long referenceId) {
        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found"));
        Notification notification = new Notification();
        notification.setRecipientUser(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse res = new NotificationResponse();
        res.setId(notification.getId());
        res.setType(notification.getType());
        res.setTitle(notification.getTitle());
        res.setMessage(notification.getMessage());
        res.setReferenceType(notification.getReferenceType());
        res.setReferenceId(notification.getReferenceId());
        res.setRead(notification.isRead());
        res.setCreatedAt(notification.getCreatedAt());
        return res;
    }
}
