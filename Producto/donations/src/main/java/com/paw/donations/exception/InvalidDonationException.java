package com.paw.donations.exception;

// Datos de la donación inválidos: ni campaña ni ONG, o el destino no está activo.
public class InvalidDonationException extends RuntimeException {
    public InvalidDonationException(String message) {
        super(message);
    }
}
