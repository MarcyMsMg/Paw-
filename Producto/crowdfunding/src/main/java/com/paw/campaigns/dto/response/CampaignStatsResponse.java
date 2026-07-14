package com.paw.campaigns.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CampaignStatsResponse(
        long activeCampaigns,
        long finishedCampaigns,
        BigDecimal totalGoalAmount,
        BigDecimal totalRaisedAmount,
        BigDecimal averageProgress,
        List<CampaignProgressItem> campaignProgress
) {
}