package com.paw.adoptions.client;

import java.util.UUID;

import com.paw.adoptions.security.AccountStatus;
import com.paw.adoptions.security.UserRole;

public record UserAccessResponse(
        UUID id,
        String email,
        UserRole role,
        AccountStatus status,
        String ngoName,
        String location
) {
}
