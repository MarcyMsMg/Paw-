package com.paw.api_gateway.filter;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.paw.api_gateway.config.GatewayProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsEdgeFilter extends OncePerRequestFilter {

    private static final String ALLOWED_METHODS = "GET,POST,PUT,PATCH,DELETE,OPTIONS";
    private static final String DEFAULT_ALLOWED_HEADERS = "Authorization,Content-Type,X-Requested-With,X-Internal-Api-Key";
    private static final String EXPOSED_HEADERS = "Authorization,Location";

    private final GatewayProperties properties;

    public CorsEdgeFilter(GatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        boolean allowedOrigin = origin != null && isAllowedOrigin(origin);

        if (allowedOrigin) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
            response.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
            response.setHeader("Access-Control-Allow-Headers", allowedHeaders(request));
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Expose-Headers", EXPOSED_HEADERS);
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        if (HttpMethod.OPTIONS.matches(request.getMethod())
                && request.getHeader("Access-Control-Request-Method") != null) {
            response.setStatus(allowedOrigin ? HttpServletResponse.SC_NO_CONTENT : HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String allowedHeaders(HttpServletRequest request) {
        String requestedHeaders = request.getHeader("Access-Control-Request-Headers");
        if (requestedHeaders == null || requestedHeaders.isBlank()) {
            return DEFAULT_ALLOWED_HEADERS;
        }
        return requestedHeaders;
    }

    private boolean isAllowedOrigin(String origin) {
        return properties.getCors().getAllowedOrigins().contains("*")
                || properties.getCors().getAllowedOrigins().contains(origin);
    }
}