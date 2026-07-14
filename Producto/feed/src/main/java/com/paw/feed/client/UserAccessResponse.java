package com.paw.feed.client;

import java.util.UUID;

import com.paw.feed.security.AccountStatus;
import com.paw.feed.security.UserRole;

public record UserAccessResponse(
        UUID id,
        String email,
        UserRole role,
        AccountStatus status,
        String ngoName,
        String location
) {
}
