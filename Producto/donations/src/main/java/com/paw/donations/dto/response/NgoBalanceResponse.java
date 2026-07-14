package com.paw.donations.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// Cuánto se le debe a una ONG:
//   balanceOwed = totalDonated (donaciones APROBADAS de campañas COMPLETED) - totalPaidOut (ya transferido)
// campaigns: desglose de las campañas finalizadas que componen ese saldo.
public record NgoBalanceResponse(
        UUID ngoId,
        String ngoName,
        BigDecimal totalDonated,
        BigDecimal totalPaidOut,
        BigDecimal balanceOwed,
        List<CampaignBalanceResponse> campaigns
) {
}
