package com.paw.adoptions.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FormTemplateResponse(
        UUID id,
        String name,
        String description,
        boolean active,
        int revision,
        int maxCustomFields,
        long assignedAnimals,
        List<FormFieldResponse> fields,
        Instant createdAt,
        Instant updatedAt
) {
}
