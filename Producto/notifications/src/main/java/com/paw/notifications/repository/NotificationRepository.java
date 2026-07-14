package com.paw.notifications.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.paw.notifications.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {
    boolean existsByEventId(String eventId);
    Optional<Notification> findByEventId(String eventId);
    long countByRecipientUserIdAndReadAtIsNull(UUID recipientUserId);
}