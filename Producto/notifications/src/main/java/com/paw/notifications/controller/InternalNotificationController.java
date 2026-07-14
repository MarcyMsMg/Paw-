package com.paw.notifications.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.notifications.common.ApiResponse;
import com.paw.notifications.dto.NotificationEvent;
import com.paw.notifications.dto.NotificationResponse;
import com.paw.notifications.exception.ApiException;
import com.paw.notifications.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications/internal")
public class InternalNotificationController {
    private final NotificationService notificationService;
    private final String internalApiKey;

    public InternalNotificationController(
            NotificationService notificationService,
            @Value("${paw.notifications.internal-api-key}") String internalApiKey
    ) {
        this.notificationService = notificationService;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping
    public ApiResponse<NotificationResponse> create(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String providedApiKey,
            @Valid @RequestBody NotificationEvent event
    ) {
        validateApiKey(providedApiKey);
        return ApiResponse.ok("Notification created", notificationService.createFromEvent(event));
    }

    private void validateApiKey(String providedApiKey) {
        if (providedApiKey == null || !MessageDigest.isEqual(
                internalApiKey.getBytes(StandardCharsets.UTF_8),
                providedApiKey.getBytes(StandardCharsets.UTF_8)
        )) {
            throw ApiException.unauthorized("Invalid internal API key");
        }
    }
}