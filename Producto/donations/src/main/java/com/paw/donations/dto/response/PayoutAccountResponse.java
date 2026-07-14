package com.paw.donations.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PayoutAccountResponse(
        UUID id,
        UUID ngoId,
        String holderName,
        String rut,
        String bankName,
        String accountType,
        String accountNumber,
        String email,
        Instant updatedAt
) {
}
