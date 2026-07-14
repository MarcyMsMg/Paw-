package com.paw.notifications.dto;

import java.time.Instant;
import java.util.UUID;

import com.paw.notifications.domain.EntityType;
import com.paw.notifications.domain.NotificationType;
import com.paw.notifications.domain.RecipientRole;

public record NotificationResponse(
        UUID id,
        UUID recipientUserId,
        RecipientRole recipientRole,
        NotificationType type,
        String title,
        String message,
        EntityType entityType,
        UUID entityId,
        String redirectUrl,
        String metadata,
        Instant readAt,
        Instant createdAt
) {
}