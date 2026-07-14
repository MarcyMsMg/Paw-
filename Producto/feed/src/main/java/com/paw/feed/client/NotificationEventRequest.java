package com.paw.feed.client;

import java.time.Instant;
import java.util.UUID;

public record NotificationEventRequest(
        String eventId,
        String type,
        Instant occurredAt,
        UUID recipientUserId,
        String recipientRole,
        String title,
        String message,
        String entityType,
        UUID entityId,
        String redirectUrl,
        String metadata
) {
}