package com.paw.campaigns.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCampaignRequest(
        @NotNull UUID ngoId,
        @NotBlank @Size(min = 3, max = 120) String title,
        @NotBlank @Size(min = 20, max = 2000) String description,
        @Size(max = 1000) String bannerUrl,
        @Size(max = 1000) String videoUrl,
        @Size(max = 80) String category,
        @NotNull @DecimalMin(value = "1000.0") BigDecimal goalAmount,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
