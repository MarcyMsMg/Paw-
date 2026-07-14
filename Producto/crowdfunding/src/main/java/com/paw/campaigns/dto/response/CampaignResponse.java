package com.paw.campaigns.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.paw.campaigns.enums.CampaignStatus;

public record CampaignResponse(
        UUID id,
        UUID ngoId,
        String title,
        String description,
        String bannerUrl,
        String videoUrl,
        String category,
        BigDecimal goalAmount,
        BigDecimal raisedAmount,
        LocalDate startDate,
        LocalDate endDate,
        CampaignStatus status,
        Instant createdAt
) {
}
