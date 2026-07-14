package com.paw.api_gateway.proxy;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.paw.api_gateway.config.GatewayProperties;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class GatewayProxyController {

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "content-length",
            "host",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade"
    );

    private static final Set<String> BROWSER_CORS_HEADERS = Set.of(
            "access-control-request-headers",
            "access-control-request-method",
            "accept-encoding",
            "origin"
    );

    private static final Set<String> RESPONSE_HEADERS_TO_FORWARD = Set.of(
            "cache-control",
            "content-disposition",
            "content-encoding",
            "content-type",
            "etag",
            "last-modified",
            "location",
            "pragma",
            "set-cookie",
            "www-authenticate"
    );

    private final GatewayProperties properties;
    private final RestTemplate restTemplate;

    public GatewayProxyController(GatewayProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @RequestMapping(
            path = {"/api/auth/**", "/api/users/**", "/api/ngos/**", "/api/admin/**"},
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS
            }
    )
    ResponseEntity<byte[]> forwardToUsers(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(request, body, properties.getServices().getUsersUrl());
    }

    @RequestMapping(
            path = "/api/adoptions/**",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS
            }
    )
    ResponseEntity<byte[]> forwardToAdoptions(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(request, body, properties.getServices().getAdoptionsUrl());
    }



    @RequestMapping(
            path = "/api/notifications/**",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS
            }
    )
    ResponseEntity<byte[]> forwardToNotifications(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(request, body, properties.getServices().getNotificationsUrl());
    }
    @RequestMapping(
            path = "/api/feed/**",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS
            }
    )
    ResponseEntity<byte[]> forwardToFeed(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(request, body, properties.getServices().getFeedUrl());
    }
    @RequestMapping(
            path = "/api/campaigns/**",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS
            }
    )
    ResponseEntity<byte[]> forwardToCampaigns(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(request, body, properties.getServices().getCampaignsUrl());
    }

    // Donaciones expone /api/donations/**, /api/admin/payouts/** (panel admin) y
    // /api/payout-accounts/** (datos bancarios de las ONG). El patrÃƒÆ’Ã‚Â³n
    // /api/admin/payouts/** es mÃƒÆ’Ã‚Â¡s especÃƒÆ’Ã‚Â­fico que /api/admin/** (que va a Users),
    // asÃƒÆ’Ã‚Â­ que Spring lo enruta aquÃƒÆ’Ã‚Â­.
    @RequestMapping(
            path = {"/api/donations/**", "/api/admin/payouts/**", "/api/payout-accounts/**"},
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS
            }
    )
    ResponseEntity<byte[]> forwardToDonations(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return forward(request, body, properties.getServices().getDonationsUrl());
    }

    private ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body, String serviceBaseUrl) {
        String targetUrl = buildTargetUrl(request, serviceBaseUrl);
        HttpHeaders requestHeaders = copyRequestHeaders(request);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpEntity<byte[]> entity = new HttpEntity<>(body, requestHeaders);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(targetUrl, method, entity, byte[].class);
            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(copyResponseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .headers(copyResponseHeaders(ex.getResponseHeaders()))
                    .body(ex.getResponseBodyAsByteArray());
        } catch (ResourceAccessException ex) {
            return serviceUnavailable();
        }
    }

    private String buildTargetUrl(HttpServletRequest request, String serviceBaseUrl) {
        String baseUrl = serviceBaseUrl.endsWith("/")
                ? serviceBaseUrl.substring(0, serviceBaseUrl.length() - 1)
                : serviceBaseUrl;

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path(request.getRequestURI());

        if (request.getQueryString() != null) {
            builder.query(request.getQueryString());
        }

        return builder.build(true).toUriString();
    }

    private HttpHeaders copyRequestHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> names = request.getHeaderNames();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (shouldSkipHeader(name)) {
                continue;
            }

            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                headers.add(name, values.nextElement());
            }
        }

        headers.set("X-Forwarded-Host", request.getHeader("Host"));
        headers.set("X-Forwarded-Proto", request.getScheme());
        headers.set("X-Forwarded-Prefix", "/api");
        return headers;
    }

    private HttpHeaders copyResponseHeaders(HttpHeaders source) {
        HttpHeaders headers = new HttpHeaders();
        if (source == null) {
            return headers;
        }

        source.forEach((name, values) -> {
            String lowerCaseName = name.toLowerCase(Locale.ROOT);
            if (RESPONSE_HEADERS_TO_FORWARD.contains(lowerCaseName)) {
                headers.put(name, values);
            }
        });

        return headers;
    }

    private boolean shouldSkipHeader(String name) {
        String lowerCaseName = name.toLowerCase(Locale.ROOT);
        return HOP_BY_HOP_HEADERS.contains(lowerCaseName) || BROWSER_CORS_HEADERS.contains(lowerCaseName);
    }

    private ResponseEntity<byte[]> serviceUnavailable() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"success\":false,\"message\":\"Target microservice is unavailable\"}";
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .headers(headers)
                .body(body.getBytes(StandardCharsets.UTF_8));
    }
}
