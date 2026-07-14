package com.paw.donations.exception;

// Falla al comunicarse con MercadoPago (crear preferencia, consultar pago, etc.).
public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message) {
        super(message);
    }
}
