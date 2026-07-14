package com.paw.notifications.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paw.notifications.common.ApiResponse;
import com.paw.notifications.domain.NotificationType;
import com.paw.notifications.dto.NotificationResponse;
import com.paw.notifications.dto.UnreadCountResponse;
import com.paw.notifications.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ApiResponse<List<NotificationResponse>> listMine(
            @RequestParam(required = false) Boolean unread,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.ok(notificationService.listMine(unread, type, limit));
    }

    @GetMapping("/me/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount() {
        return ApiResponse.ok(notificationService.unreadCount());
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable UUID notificationId) {
        return ApiResponse.ok("Notification marked as read", notificationService.markAsRead(notificationId));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.ok("Notifications marked as read", null);
    }

    @DeleteMapping("/{notificationId}")
    public ApiResponse<Void> delete(@PathVariable UUID notificationId) {
        notificationService.delete(notificationId);
        return ApiResponse.ok("Notification deleted", null);
    }
}