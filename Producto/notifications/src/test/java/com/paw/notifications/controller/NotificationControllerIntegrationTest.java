package com.paw.notifications.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.paw.notifications.domain.EntityType;
import com.paw.notifications.domain.NotificationType;
import com.paw.notifications.domain.RecipientRole;
import com.paw.notifications.dto.NotificationResponse;
import com.paw.notifications.dto.UnreadCountResponse;
import com.paw.notifications.exception.ApiException;
import com.paw.notifications.security.JwtService;
import com.paw.notifications.service.NotificationService;

@WebMvcTest({NotificationController.class, InternalNotificationController.class})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "paw.notifications.internal-api-key=test-key")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void listarMisNotificaciones_devuelveListaFiltrada() throws Exception {
        // Arrange
        when(notificationService.listMine(true, NotificationType.SYSTEM_ALERT, 5)).thenReturn(List.of(notificationResponse()));

        // Act + Assert
        mockMvc.perform(get("/api/notifications/me")
                        .param("unread", "true")
                        .param("type", "SYSTEM_ALERT")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Alerta"));
    }

    @Test
    void contadorNoLeidas_devuelveCount() throws Exception {
        // Arrange
        when(notificationService.unreadCount()).thenReturn(new UnreadCountResponse(3));

        // Act + Assert
        mockMvc.perform(get("/api/notifications/me/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(3));
    }

    @Test
    void marcarComoLeida_inexistente_devuelve404() throws Exception {
        // Arrange
        when(notificationService.markAsRead(any(UUID.class))).thenThrow(ApiException.notFound("Notification not found"));

        // Act + Assert
        mockMvc.perform(patch("/api/notifications/{id}/read", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void crearInterna_sinApiKey_devuelve401YNoCrea() throws Exception {
        // Arrange
        String body = validInternalEventJson();

        // Act + Assert
        mockMvc.perform(post("/api/notifications/internal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        verify(notificationService, never()).createFromEvent(any());
    }

    @Test
    void crearInterna_conApiKeyValida_devuelveOk() throws Exception {
        // Arrange
        when(notificationService.createFromEvent(any())).thenReturn(notificationResponse());

        // Act + Assert
        mockMvc.perform(post("/api/notifications/internal")
                        .header("X-Internal-Api-Key", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validInternalEventJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("SYSTEM_ALERT"));
    }

    private String validInternalEventJson() {
        return """
                {
                  "eventId": "evt-1",
                  "type": "SYSTEM_ALERT",
                  "recipientUserId": "11111111-1111-1111-1111-111111111111",
                  "recipientRole": "NATURAL_PERSON",
                  "title": "Alerta",
                  "message": "Mensaje interno",
                  "entityType": "SYSTEM",
                  "redirectUrl": "/notificaciones"
                }
                """;
    }

    private NotificationResponse notificationResponse() {
        return new NotificationResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                RecipientRole.NATURAL_PERSON,
                NotificationType.SYSTEM_ALERT,
                "Alerta",
                "Mensaje interno",
                EntityType.SYSTEM,
                null,
                "/notificaciones",
                null,
                null,
                Instant.now()
        );
    }
}
