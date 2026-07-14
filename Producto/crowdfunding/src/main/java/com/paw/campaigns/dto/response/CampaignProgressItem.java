package com.paw.campaigns.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.paw.campaigns.enums.CampaignStatus;

public record CampaignProgressItem(
        UUID id,
        String title,
        BigDecimal goalAmount,
        BigDecimal raisedAmount,
        BigDecimal progress,
        CampaignStatus status
) {
}