package com.paw.notifications.domain;

public enum RecipientRole {
    ADMIN,
    NGO,
    NATURAL_PERSON;

    public static RecipientRole from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        return RecipientRole.valueOf(value.trim().toUpperCase().replace("ROLE_", ""));
    }
}