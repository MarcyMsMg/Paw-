package com.paw.donations.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

// El admin registra una transferencia ya realizada a una ONG.
public record CreatePayoutRequest(
        @NotNull UUID ngoId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        String reference,
        String note
) {
}
