package com.paw.donations.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Comprobante de una donación aprobada, enriquecido con los nombres del donante,
// la campaña y la ONG (leídos de los otros microservicios; quedan en null si no se
// pudieron resolver).
public record ReceiptResponse(
        String receiptNumber,
        UUID donationId,
        UUID donorId,
        String donorName,
        String donorEmail,
        UUID campaignId,
        String campaignTitle,
        UUID ngoId,
        String ngoName,
        BigDecimal amount,
        String paymentMethod,
        Instant paidAt
) {
}
