package com.paw.feed.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationsClient {

    private final RestTemplate restTemplate;
    private final String notificationsBaseUrl;
    private final String internalApiKey;

    public NotificationsClient(
            RestTemplate restTemplate,
            @Value("${paw.services.notifications.base-url}") String notificationsBaseUrl,
            @Value("${paw.services.notifications.internal-api-key:}") String internalApiKey
    ) {
        this.restTemplate = restTemplate;
        this.notificationsBaseUrl = notificationsBaseUrl;
        this.internalApiKey = internalApiKey;
    }

    public void send(NotificationEventRequest event) {
        if (event == null) {
            log.warn("Notification event is null. Skipping notification.");
            return;
        }

        if (internalApiKey == null || internalApiKey.isBlank()) {
            log.warn("INTERNAL_API_KEY is missing. Skipping notification event {}", event.eventId());
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", internalApiKey);

        try {
            restTemplate.exchange(
                    notificationsBaseUrl + "/notifications/internal",
                    HttpMethod.POST,
                    new HttpEntity<>(event, headers),
                    Void.class
            );

            log.info("Notification event sent: {}", event.eventId());

        } catch (RestClientException exception) {
            log.warn("Could not send notification event {}: {}", event.eventId(), exception.getMessage());
        }
    }
}