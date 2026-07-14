package com.paw.users.security;

import java.util.UUID;

import com.paw.users.enums.UserRole;

public record AuthenticatedUser(
        UUID id,
        String email,
        UserRole role
) {
}
