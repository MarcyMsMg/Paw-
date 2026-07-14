package com.paw.campaigns.exception;

// Se lanza cuando la ONG indicada no existe o no está activa para crear campañas.
public class NgoNotEligibleException extends RuntimeException {
    public NgoNotEligibleException(String message) {
        super(message);
    }
}
