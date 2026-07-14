package com.paw.campaigns.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

// Actualizacion parcial: solo se modifican los campos que vienen distintos de null.
// OJO: goalAmount (la meta) NO es editable a propósito — el monto es inmutable tras crear.
public record UpdateCampaignRequest(
        @Size(min = 3, max = 120) String title,
        @Size(min = 20, max = 2000) String description,
        @Size(max = 1000) String bannerUrl,
        @Size(max = 1000) String videoUrl,
        @Size(max = 80) String category,
        LocalDate startDate,
        LocalDate endDate
) {
}
