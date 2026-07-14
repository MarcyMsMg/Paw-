package com.paw.api_gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.paw.api_gateway.config.GatewayProperties;

class CorsEdgeFilterTest {

    @Test
    void doFilterInternal_shouldShortCircuitAllowedPreflight() throws Exception {
        // Arrange
        GatewayProperties properties = new GatewayProperties();
        properties.getCors().setAllowedOrigins(List.of("http://localhost:5173"));
        CorsEdgeFilter filter = new CorsEdgeFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/adoptions/animals");
        request.addHeader("Origin", "http://localhost:5173");
        request.addHeader("Access-Control-Request-Method", "GET");
        request.addHeader("Access-Control-Request-Headers", "Authorization");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        assertEquals(204, response.getStatus());
        assertEquals("http://localhost:5173", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("Authorization", response.getHeader("Access-Control-Allow-Headers"));
    }

    @Test
    void doFilterInternal_shouldContinueChainForNonPreflightRequest() throws Exception {
        // Arrange
        GatewayProperties properties = new GatewayProperties();
        properties.getCors().setAllowedOrigins(List.of("*"));
        CorsEdgeFilter filter = new CorsEdgeFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/feed/posts");
        request.addHeader("Origin", "http://example.test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        filter.doFilterInternal(request, response, chain);

        // Assert
        assertEquals("http://example.test", response.getHeader("Access-Control-Allow-Origin"));
        assertNotNull(chain.getRequest());
    }
}