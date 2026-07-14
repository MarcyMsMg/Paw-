package com.paw.notifications.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.paw.notifications.common.ApiResponse;
import com.paw.notifications.exception.ApiException;

@Component
public class UsersClient {
    private final RestTemplate restTemplate;
    private final String usersBaseUrl;
    private final String internalApiKey;

    public UsersClient(
            RestTemplate restTemplate,
            @Value("${paw.services.users.base-url}") String usersBaseUrl,
            @Value("${paw.services.users.internal-api-key}") String internalApiKey
    ) {
        this.restTemplate = restTemplate;
        this.usersBaseUrl = usersBaseUrl;
        this.internalApiKey = internalApiKey;
    }

    public UserAccessResponse findAccess(UUID userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", internalApiKey);
        try {
            ResponseEntity<ApiResponse<UserAccessResponse>> response = restTemplate.exchange(
                    usersBaseUrl + "/internal/users/" + userId + "/access",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );
            ApiResponse<UserAccessResponse> body = response.getBody();
            if (body == null || !body.success() || body.data() == null) {
                throw ApiException.serviceUnavailable("Users service returned an invalid response");
            }
            return body.data();
        } catch (HttpClientErrorException.NotFound exception) {
            throw ApiException.unauthorized("Authenticated user no longer exists");
        } catch (HttpClientErrorException.Unauthorized exception) {
            throw ApiException.serviceUnavailable("Users service rejected the internal credentials");
        } catch (RestClientException exception) {
            throw ApiException.serviceUnavailable("Users service is unavailable");
        }
    }
}