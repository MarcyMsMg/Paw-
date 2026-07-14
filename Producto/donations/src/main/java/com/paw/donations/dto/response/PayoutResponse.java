package com.paw.donations.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayoutResponse(
        UUID id,
        UUID ngoId,
        BigDecimal amount,
        String reference,
        String note,
        Instant createdAt
) {
}
