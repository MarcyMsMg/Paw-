package com.paw.donations.enums;

public enum DonationStatus {
    PENDING,        // PENDIENTE — esperando el pago
    APPROVED,       // APROBADO — pago confirmado por MercadoPago
    REJECTED,       // RECHAZADO — pago rechazado/cancelado
    REFUNDED,       // DEVUELTO — se reembolsó un pago que estaba aprobado
    CHARGED_BACK,   // CONTRACARGO — el banco/usuario revirtió el cobro
    EXPIRED         // EXPIRADO — el donante nunca completó el pago (PENDING abandonado)
}
