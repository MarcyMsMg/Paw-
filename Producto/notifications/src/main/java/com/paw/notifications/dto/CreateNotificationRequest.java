package com.paw.notifications.dto;

import java.util.UUID;

import com.paw.notifications.domain.EntityType;
import com.paw.notifications.domain.NotificationType;
import com.paw.notifications.domain.RecipientRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull UUID recipientUserId,
        @NotNull RecipientRole recipientRole,
        @NotNull NotificationType type,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 1000) String message,
        @NotNull EntityType entityType,
        UUID entityId,
        @Size(max = 500) String redirectUrl,
        String metadata
) {
}