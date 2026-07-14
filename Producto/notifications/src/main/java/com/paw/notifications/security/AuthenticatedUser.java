package com.paw.notifications.security;

import java.util.UUID;

import com.paw.notifications.domain.RecipientRole;

public record AuthenticatedUser(UUID id, String email, RecipientRole role) {
}