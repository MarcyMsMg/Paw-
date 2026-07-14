package com.paw.notifications.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paw.notifications.domain.Notification;
import com.paw.notifications.domain.NotificationType;
import com.paw.notifications.dto.CreateNotificationRequest;
import com.paw.notifications.dto.NotificationEvent;
import com.paw.notifications.dto.NotificationResponse;
import com.paw.notifications.dto.UnreadCountResponse;
import com.paw.notifications.exception.ApiException;
import com.paw.notifications.repository.NotificationRepository;
import com.paw.notifications.security.AuthenticatedUser;
import com.paw.notifications.security.CurrentUserProvider;

import jakarta.persistence.criteria.Predicate;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final CurrentUserProvider currentUserProvider;
    private final UserAccessValidator userAccessValidator;

    public NotificationService(
            NotificationRepository repository,
            CurrentUserProvider currentUserProvider,
            UserAccessValidator userAccessValidator
    ) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
        this.userAccessValidator = userAccessValidator;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listMine(Boolean unread, NotificationType type, Integer limit) {
        AuthenticatedUser user = activeCurrentUser();
        int size = normalizeLimit(limit);
        Specification<Notification> specification = mineSpec(user.id(), unread, type);
        return repository.findAll(specification, PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount() {
        AuthenticatedUser user = activeCurrentUser();
        return new UnreadCountResponse(repository.countByRecipientUserIdAndReadAtIsNull(user.id()));
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        AuthenticatedUser user = activeCurrentUser();
        Notification notification = findOwned(notificationId, user.id());
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
        }
        return toResponse(notification);
    }

    @Transactional
    public void markAllAsRead() {
        AuthenticatedUser user = activeCurrentUser();
        List<Notification> unread = repository.findAll(mineSpec(user.id(), true, null));
        Instant now = Instant.now();
        unread.forEach(notification -> notification.setReadAt(now));
    }

    @Transactional
    public void delete(UUID notificationId) {
        AuthenticatedUser user = activeCurrentUser();
        Notification notification = findOwned(notificationId, user.id());
        repository.delete(notification);
    }

    @Transactional
    public NotificationResponse createInternal(CreateNotificationRequest request) {
        String eventId = "internal:" + request.recipientUserId() + ":" + request.type() + ":" + request.entityType() + ":" + (request.entityId() == null ? UUID.randomUUID() : request.entityId());
        return createFromEvent(new NotificationEvent(
                eventId,
                request.type(),
                Instant.now(),
                request.recipientUserId(),
                request.recipientRole(),
                request.title(),
                request.message(),
                request.entityType(),
                request.entityId(),
                request.redirectUrl(),
                request.metadata()
        ));
    }

    @Transactional
    public NotificationResponse createFromEvent(NotificationEvent event) {
        return repository.findByEventId(event.eventId())
                .map(this::toResponse)
                .orElseGet(() -> toResponse(repository.save(toEntity(event))));
    }

    private AuthenticatedUser activeCurrentUser() {
        AuthenticatedUser user = currentUserProvider.get();
        userAccessValidator.requireActive(user);
        return user;
    }

    private Notification findOwned(UUID notificationId, UUID userId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!notification.getRecipientUserId().equals(userId)) {
            throw ApiException.forbidden("You cannot access this notification");
        }
        return notification;
    }

    private Specification<Notification> mineSpec(UUID userId, Boolean unread, NotificationType type) {
        return (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("recipientUserId"), userId));
            if (Boolean.TRUE.equals(unread)) {
                predicates.add(cb.isNull(root.get("readAt")));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 50;
        }
        return Math.max(1, Math.min(limit, 100));
    }

    private Notification toEntity(NotificationEvent event) {
        Notification notification = new Notification();
        notification.setEventId(event.eventId());
        notification.setRecipientUserId(event.recipientUserId());
        notification.setRecipientRole(event.recipientRole());
        notification.setType(event.type());
        notification.setTitle(event.title());
        notification.setMessage(event.message());
        notification.setEntityType(event.entityType());
        notification.setEntityId(event.entityId());
        notification.setRedirectUrl(event.redirectUrl());
        notification.setMetadata(event.metadata());
        notification.setCreatedAt(event.occurredAt() == null ? Instant.now() : event.occurredAt());
        return notification;
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getRecipientRole(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getEntityType(),
                notification.getEntityId(),
                notification.getRedirectUrl(),
                notification.getMetadata(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}