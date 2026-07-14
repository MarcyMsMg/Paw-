package com.paw.adoptions.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.paw.adoptions.domain.AdoptionApplicationStatus;

public record AdoptionApplicationResponse(
        UUID id,
        UUID animalId,
        String animalName,
        String animalPhotoUrl,
        String fullName,
        String email,
        String phone,
        String address,
        String housingType,
        String otherAnimals,
        String motivation,
        String availability,
        String previousExperience,
        UUID formTemplateId,
        Integer formTemplateRevision,
        List<ApplicationAnswerResponse> customAnswers,
        AdoptionApplicationStatus status,
        String ngoResponse,
        Instant createdAt,
        Instant updatedAt
) {
}
