package com.paw.donations.dto.response;

import java.util.UUID;

import com.paw.donations.enums.DonationStatus;

// Lo que devuelve POST /api/donations: el id de la donación creada (en PENDING) y la
// URL de MercadoPago a la que el frontend debe redirigir para completar el pago.
public record CheckoutResponse(
        UUID donationId,
        DonationStatus status,
        String checkoutUrl
) {
}
