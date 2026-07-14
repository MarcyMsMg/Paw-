package com.paw.donations.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

// Cuánto aporta una campaña finalizada (COMPLETED) al saldo que se le debe a su ONG.
public record CampaignBalanceResponse(
        UUID campaignId,
        String campaignTitle,
        BigDecimal amount
) {
}
