package com.paw.campaigns.exception;

// Se lanza cuando no se puede contactar al microservicio de Usuarios.
public class UsersServiceUnavailableException extends RuntimeException {
    public UsersServiceUnavailableException(String message) {
        super(message);
    }
}
