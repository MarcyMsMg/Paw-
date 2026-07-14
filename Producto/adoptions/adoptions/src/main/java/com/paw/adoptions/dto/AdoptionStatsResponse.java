package com.paw.adoptions.dto;

import java.util.Map;

public record AdoptionStatsResponse(
        long totalApplications,
        long pendingApplications,
        long acceptedApplications,
        long rejectedApplications,
        Map<String, Long> applicationsByStatus,
        long animalsAvailable,
        long animalsAdopted,
        Map<String, Long> animalsByStatus
) {
}