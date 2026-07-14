package com.paw.donations.exception;

// No se pudo contactar a otro microservicio (Campañas o Usuarios).
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
