package com.paw.donations.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

// MVP: las donaciones siempre van a una campaña. El ngoId se deriva de la campaña.
// (El aporte directo a una ONG queda para una etapa posterior.)
public record CreateDonationRequest(
        @NotNull UUID donorId,
        @NotNull UUID campaignId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount
) {
}
