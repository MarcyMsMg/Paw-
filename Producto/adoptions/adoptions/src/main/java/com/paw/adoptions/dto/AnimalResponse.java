package com.paw.adoptions.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.paw.adoptions.domain.AnimalStatus;

public record AnimalResponse(
        UUID id,
        UUID ngoId,
        String name,
        String species,
        String age,
        String sex,
        String size,
        String location,
        String healthStatus,
        String description,
        String adoptionRequirements,
        UUID formTemplateId,
        List<String> photoUrls,
        AnimalStatus status,
        boolean published,
        Instant createdAt,
        Instant updatedAt
) {
}
