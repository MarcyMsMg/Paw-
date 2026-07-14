package com.paw.notifications.dto;

import java.time.Instant;
import java.util.UUID;

import com.paw.notifications.domain.EntityType;
import com.paw.notifications.domain.NotificationType;
import com.paw.notifications.domain.RecipientRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationEvent(
        @NotBlank @Size(max = 160) String eventId,
        @NotNull NotificationType type,
        Instant occurredAt,
        @NotNull UUID recipientUserId,
        @NotNull RecipientRole recipientRole,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 1000) String message,
        @NotNull EntityType entityType,
        UUID entityId,
        @Size(max = 500) String redirectUrl,
        String metadata
) {
}