package com.paw.donations.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record DonationStatsResponse(
        BigDecimal totalDonated,
        BigDecimal totalRaised,
        long approvedDonations,
        long pendingDonations,
        long rejectedDonations,
        Map<String, BigDecimal> donationsByMonth
) {
}