package com.paw.donations.security;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, UserRole role) {
}
