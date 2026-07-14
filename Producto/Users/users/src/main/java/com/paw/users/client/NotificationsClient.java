package com.paw.users.client;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.paw.users.enums.UserRole;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationsClient {

    private final RestTemplate restTemplate;
    private final String notificationsBaseUrl;
    private final String internalApiKey;

    public NotificationsClient(
            RestTemplate restTemplate,
            @Value("${pawplus.services.notifications.base-url:http://localhost:8086/api}") String notificationsBaseUrl,
            @Value("${pawplus.services.notifications.internal-api-key:${pawplus.internal-api-key:}}") String internalApiKey
    ) {
        this.restTemplate = restTemplate;
        this.notificationsBaseUrl = notificationsBaseUrl;
        this.internalApiKey = internalApiKey;
    }

    public void send(String eventId, String type, UUID recipientUserId, UserRole recipientRole,
                     String title, String message, String entityType, UUID entityId, String redirectUrl, String metadata) {

        if (internalApiKey == null || internalApiKey.isBlank()) {
            log.warn("INTERNAL_API_KEY is missing. Skipping notification event {}", eventId);
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", eventId);
        payload.put("type", type);
        payload.put("occurredAt", Instant.now().toString());
        payload.put("recipientUserId", recipientUserId);
        payload.put("recipientRole", recipientRole.name());
        payload.put("title", title);
        payload.put("message", message);
        payload.put("entityType", entityType);
        payload.put("entityId", entityId);
        payload.put("redirectUrl", redirectUrl);
        payload.put("metadata", metadata);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Api-Key", internalApiKey);

        try {
            restTemplate.postForEntity(
                    notificationsBaseUrl + "/notifications/internal",
                    new HttpEntity<>(payload, headers),
                    Void.class
            );

            log.info("Notification event sent: {}", eventId);

        } catch (RestClientException exception) {
            log.warn("Could not send notification event {}: {}", eventId, exception.getMessage());
        }
    }
}