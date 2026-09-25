package com.creatorverse.notification.repository;

import com.creatorverse.notification.entity.Notification;
import com.creatorverse.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientUserOrderByCreatedAtDesc(User recipientUser, Pageable pageable);

    long countByRecipientUserAndIsReadFalse(User recipientUser);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientUser = :user AND n.isRead = false")
    void markAllAsReadByRecipientUser(@Param("user") User user);
}
