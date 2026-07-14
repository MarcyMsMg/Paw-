package com.paw.users.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.users.dto.response.ApiResponse;
import com.paw.users.dto.response.InternalUserAccessResponse;
import com.paw.users.exception.InvalidInternalApiKeyException;
import com.paw.users.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @Value("${pawplus.internal-api-key}")
    private String internalApiKey;

    @GetMapping("/{userId}/access")
    public ApiResponse<InternalUserAccessResponse> findAccess(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String providedApiKey
    ) {
        validateApiKey(providedApiKey);
        return new ApiResponse<>(true, "User access found", userService.findAccessById(userId));
    }

    private void validateApiKey(String providedApiKey) {
        if (providedApiKey == null || !MessageDigest.isEqual(
                internalApiKey.getBytes(StandardCharsets.UTF_8),
                providedApiKey.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new InvalidInternalApiKeyException("Invalid internal API key");
        }
    }
}
