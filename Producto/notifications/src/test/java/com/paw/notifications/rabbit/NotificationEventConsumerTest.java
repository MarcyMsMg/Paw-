package com.paw.notifications.rabbit;

import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paw.notifications.domain.EntityType;
import com.paw.notifications.domain.NotificationType;
import com.paw.notifications.domain.RecipientRole;
import com.paw.notifications.dto.NotificationEvent;
import com.paw.notifications.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock private NotificationService notificationService;

    @Test
    void consume_shouldDelegateEventToNotificationService() {
        // Arrange
        NotificationEventConsumer consumer = new NotificationEventConsumer(notificationService);
        NotificationEvent event = new NotificationEvent(
                "event-1",
                NotificationType.SYSTEM_ALERT,
                Instant.now(),
                UUID.randomUUID(),
                RecipientRole.ADMIN,
                "Titulo",
                "Mensaje",
                EntityType.SYSTEM,
                null,
                "/notificaciones",
                "{}"
        );

        // Act
        consumer.consume(event);

        // Assert
        verify(notificationService).createFromEvent(event);
    }
}