package com.paw.notifications.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import com.paw.notifications.domain.EntityType;
import com.paw.notifications.domain.Notification;
import com.paw.notifications.domain.NotificationType;
import com.paw.notifications.domain.RecipientRole;
import com.paw.notifications.dto.CreateNotificationRequest;
import com.paw.notifications.dto.NotificationEvent;
import com.paw.notifications.exception.ApiException;
import com.paw.notifications.repository.NotificationRepository;
import com.paw.notifications.security.AuthenticatedUser;
import com.paw.notifications.security.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository repository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private UserAccessValidator userAccessValidator;

    private NotificationService notificationService;
    private UUID userId;
    private AuthenticatedUser currentUser;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(repository, currentUserProvider, userAccessValidator);
        userId = UUID.randomUUID();
        currentUser = new AuthenticatedUser(userId, "person@test.local", RecipientRole.NATURAL_PERSON);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listMine_shouldFilterCurrentUserAndMapNotifications() {
        // Arrange
        Notification notification = notification(userId);
        when(currentUserProvider.get()).thenReturn(currentUser);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(notification)));

        // Act
        var responses = notificationService.listMine(true, NotificationType.ADOPTION_APPLICATION_ACCEPTED, 10);

        // Assert
        assertEquals(1, responses.size());
        assertEquals(notification.getTitle(), responses.getFirst().title());
        verify(userAccessValidator).requireActive(currentUser);
    }

    @Test
    void unreadCount_shouldUseCurrentUserId() {
        // Arrange
        when(currentUserProvider.get()).thenReturn(currentUser);
        when(repository.countByRecipientUserIdAndReadAtIsNull(userId)).thenReturn(3L);

        // Act
        var response = notificationService.unreadCount();

        // Assert
        assertEquals(3L, response.count());
    }

    @Test
    void markAsRead_shouldSetReadAtOnlyForOwnedNotification() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        Notification notification = notification(userId);
        when(currentUserProvider.get()).thenReturn(currentUser);
        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        // Act
        var response = notificationService.markAsRead(notificationId);

        // Assert
        assertNotNull(notification.getReadAt());
        assertEquals(notification.getReadAt(), response.readAt());
    }

    @Test
    void markAsRead_shouldRejectNotificationsFromAnotherUser() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        Notification notification = notification(UUID.randomUUID());
        when(currentUserProvider.get()).thenReturn(currentUser);
        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> notificationService.markAsRead(notificationId));

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, exception.status());
    }

    @Test
    @SuppressWarnings("unchecked")
    void markAllAsRead_shouldSetReadAtForUnreadNotifications() {
        // Arrange
        Notification first = notification(userId);
        Notification second = notification(userId);
        when(currentUserProvider.get()).thenReturn(currentUser);
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(first, second));

        // Act
        notificationService.markAllAsRead();

        // Assert
        assertNotNull(first.getReadAt());
        assertNotNull(second.getReadAt());
    }

    @Test
    void delete_shouldRemoveOwnedNotification() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        Notification notification = notification(userId);
        when(currentUserProvider.get()).thenReturn(currentUser);
        when(repository.findById(notificationId)).thenReturn(Optional.of(notification));

        // Act
        notificationService.delete(notificationId);

        // Assert
        verify(repository).delete(notification);
    }

    @Test
    void createInternal_shouldCreateEventBackedNotification() {
        // Arrange
        CreateNotificationRequest request = new CreateNotificationRequest(
                userId, RecipientRole.NATURAL_PERSON, NotificationType.SYSTEM_ALERT,
                "Titulo", "Mensaje", EntityType.SYSTEM, null, "/notificaciones", "{}"
        );
        when(repository.findByEventId(any())).thenReturn(Optional.empty());
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = notificationService.createInternal(request);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        assertEquals(NotificationType.SYSTEM_ALERT, captor.getValue().getType());
        assertEquals(userId, response.recipientUserId());
    }

    @Test
    void createFromEvent_shouldNotDuplicateExistingEvent() {
        // Arrange
        Notification existing = notification(userId);
        existing.setEventId("event-1");
        when(repository.findByEventId("event-1")).thenReturn(Optional.of(existing));

        // Act
        var response = notificationService.createFromEvent(event("event-1"));

        // Assert
        assertEquals("event-1", existing.getEventId());
        assertEquals(existing.getTitle(), response.title());
    }

    @Test
    void createFromEvent_shouldPersistNewEvent() {
        // Arrange
        when(repository.findByEventId("event-2")).thenReturn(Optional.empty());
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = notificationService.createFromEvent(event("event-2"));

        // Assert
        assertEquals("Titulo", response.title());
        assertEquals(EntityType.ADOPTION_APPLICATION, response.entityType());
    }

    private NotificationEvent event(String eventId) {
        return new NotificationEvent(
                eventId,
                NotificationType.ADOPTION_APPLICATION_CREATED,
                Instant.parse("2026-07-02T10:00:00Z"),
                userId,
                RecipientRole.NATURAL_PERSON,
                "Titulo",
                "Mensaje",
                EntityType.ADOPTION_APPLICATION,
                UUID.randomUUID(),
                "/persona/mis-postulaciones",
                "{}"
        );
    }

    private Notification notification(UUID recipientId) {
        Notification notification = new Notification();
        notification.setEventId("event-" + UUID.randomUUID());
        notification.setRecipientUserId(recipientId);
        notification.setRecipientRole(RecipientRole.NATURAL_PERSON);
        notification.setType(NotificationType.ADOPTION_APPLICATION_ACCEPTED);
        notification.setTitle("Solicitud aceptada");
        notification.setMessage("Tu solicitud fue aceptada");
        notification.setEntityType(EntityType.ADOPTION_APPLICATION);
        notification.setEntityId(UUID.randomUUID());
        notification.setRedirectUrl("/persona/mis-postulaciones");
        notification.setMetadata("{}");
        notification.setCreatedAt(Instant.parse("2026-07-02T10:00:00Z"));
        return notification;
    }
}