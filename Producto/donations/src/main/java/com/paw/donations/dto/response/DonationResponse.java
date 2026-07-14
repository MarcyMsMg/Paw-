package com.paw.donations.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.paw.donations.enums.DonationStatus;
import com.paw.donations.enums.DonationType;

public record DonationResponse(
        UUID id,
        UUID donorId,
        UUID campaignId,
        UUID ngoId,
        DonationType type,
        BigDecimal amount,
        BigDecimal paidAmount,
        DonationStatus status,
        String paymentMethod,
        String receiptNumber,
        Instant createdAt,
        Instant paidAt
) {
}
