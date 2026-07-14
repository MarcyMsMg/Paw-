package com.paw.api_gateway.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.paw.api_gateway.config.GatewayProperties;

@ExtendWith(MockitoExtension.class)
class GatewayProxyControllerTest {

    @Mock private RestTemplate restTemplate;

    private GatewayProxyController controller;

    @BeforeEach
    void setUp() {
        GatewayProperties properties = new GatewayProperties();
        properties.getServices().setAdoptionsUrl("http://adoptions.test/");
        properties.getServices().setDonationsUrl("http://donations.test");
        controller = new GatewayProxyController(properties, restTemplate);
    }

    @Test
    void forwardToAdoptions_shouldForwardAuthAndStripBrowserCorsHeaders() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/adoptions/animals");
        request.setQueryString("species=dog");
        request.addHeader("Authorization", "Bearer token");
        request.addHeader("Origin", "http://localhost:5173");
        request.addHeader("Access-Control-Request-Method", "GET");
        request.addHeader("Host", "localhost:8080");
        request.setScheme("http");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("X-Service", "adoptions");
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        when(restTemplate.exchange(
                eq("http://adoptions.test/api/adoptions/animals?species=dog"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(new ResponseEntity<>("ok".getBytes(StandardCharsets.UTF_8), responseHeaders, HttpStatus.OK));

        // Act
        ResponseEntity<byte[]> response = controller.forwardToAdoptions(request, null);

        // Assert
        ArgumentCaptor<HttpEntity<byte[]>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        org.mockito.Mockito.verify(restTemplate).exchange(
                eq("http://adoptions.test/api/adoptions/animals?species=dog"),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(byte[].class)
        );
        HttpHeaders forwarded = entityCaptor.getValue().getHeaders();
        assertEquals("Bearer token", forwarded.getFirst("Authorization"));
        assertEquals("localhost:8080", forwarded.getFirst("X-Forwarded-Host"));
        assertNull(forwarded.getFirst("Origin"));
        assertNull(forwarded.getFirst("Access-Control-Request-Method"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst("X-Service"));
        assertNull(response.getHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    void forwardToDonations_shouldReturnServiceUnavailableWhenTargetIsDown() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/donations");
        when(restTemplate.exchange(eq("http://donations.test/api/donations"), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenThrow(new ResourceAccessException("down"));

        // Act
        ResponseEntity<byte[]> response = controller.forwardToDonations(request, "{}".getBytes(StandardCharsets.UTF_8));

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("application/json", response.getHeaders().getContentType().toString());
        assertTrue(new String(response.getBody(), StandardCharsets.UTF_8).contains("Target microservice is unavailable"));
    }
}

