package com.paw.donations.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.paw.donations.client.dto.NotificationEventRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationsClient {

    private final RestClient notificationsRestClient;
    private final String internalApiKey;

    public NotificationsClient(
            @Qualifier("notificationsRestClient") RestClient notificationsRestClient,
            @Value("${services.notifications.internal-api-key:}") String internalApiKey
    ) {
        this.notificationsRestClient = notificationsRestClient;
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

        try {
            notificationsRestClient.post()
                    .uri("/notifications/internal")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notification event sent: {}", event.eventId());

        } catch (RestClientException exception) {
            log.warn("Could not send notification event {}: {}", event.eventId(), exception.getMessage());
        }
    }
}