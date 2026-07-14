package com.paw.users.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.paw.users.enums.NgoRegistrationStatus;

public record NgoRegistrationRequestResponse(
        UUID id,
        UUID userId,
        String ngoName,
        String email,
        String description,
        String constitutionActUrl,
        String location,
        Integer foundationYear,
        Integer rescuedAnimalsCount,
        Integer volunteersCount,
        NgoRegistrationStatus status,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {
}