package com.paw.api_gateway.proxy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "paw.gateway.services.users-url=http://users.test",
        "paw.gateway.services.adoptions-url=http://adoptions.test",
        "paw.gateway.services.feed-url=http://feed.test",
        "paw.gateway.services.notifications-url=http://notifications.test",
        "paw.gateway.cors.allowed-origins=http://localhost:5173"
})
class GatewayRoutingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void loginRoute_forwardsRequestToUsersService() throws Exception {
        // Arrange
        String loginBody = "{\"email\":\"ana@test.com\",\"password\":\"Password1\"}";
        byte[] responseBody = "{\"success\":true}".getBytes(StandardCharsets.UTF_8);
        when(restTemplate.exchange(
                eq("http://users.test/api/auth/login"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        // Act + Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));

        ArgumentCaptor<HttpEntity<byte[]>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq("http://users.test/api/auth/login"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(byte[].class)
        );
    }

    @Test
    void adoptionsRoute_forwardsAuthorizationAndQueryString() throws Exception {
        // Arrange
        byte[] responseBody = "{\"success\":true,\"data\":[]}".getBytes(StandardCharsets.UTF_8);
        when(restTemplate.exchange(
                eq("http://adoptions.test/api/adoptions/animals?status=AVAILABLE"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        // Act + Assert
        mockMvc.perform(get("/api/adoptions/animals")
                        .queryParam("status", "AVAILABLE")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true,\"data\":[]}"));

        ArgumentCaptor<HttpEntity<byte[]>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq("http://adoptions.test/api/adoptions/animals?status=AVAILABLE"),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(byte[].class)
        );
        org.assertj.core.api.Assertions.assertThat(entityCaptor.getValue().getHeaders().getFirst("Authorization"))
                .isEqualTo("Bearer token");
    }

    @Test
    void feedAndNotificationsRoutes_forwardToTheirServices() throws Exception {
        // Arrange
        when(restTemplate.exchange(
                eq("http://feed.test/api/feed/posts"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(ResponseEntity.ok("{\"success\":true}".getBytes(StandardCharsets.UTF_8)));
        when(restTemplate.exchange(
                eq("http://notifications.test/api/notifications/me/unread-count"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(ResponseEntity.ok("{\"count\":3}".getBytes(StandardCharsets.UTF_8)));

        // Act + Assert
        mockMvc.perform(get("/api/feed/posts"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));
        mockMvc.perform(get("/api/notifications/me/unread-count"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"count\":3}"));
    }

    @Test
    void corsPreflight_isHandledByGatewayWithoutCallingMicroservice() throws Exception {
        // Act + Assert
        mockMvc.perform(options("/api/adoptions/animals")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Headers", "Authorization"));

        verify(restTemplate, never()).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(byte[].class));
    }

    @Test
    void unavailableTarget_returns503ControlledResponse() throws Exception {
        // Arrange
        when(restTemplate.exchange(
                eq("http://adoptions.test/api/adoptions/animals"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenThrow(new org.springframework.web.client.ResourceAccessException("down"));

        // Act + Assert
        mockMvc.perform(get("/api/adoptions/animals"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Target microservice is unavailable")));
    }
}
