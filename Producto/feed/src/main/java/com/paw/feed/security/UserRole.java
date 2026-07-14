package com.paw.feed.security;

public enum UserRole {
    ADMIN,
    NGO,
    NATURAL_PERSON;

    public static UserRole from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        return UserRole.valueOf(value.trim().toUpperCase().replace("ROLE_", ""));
    }
}
