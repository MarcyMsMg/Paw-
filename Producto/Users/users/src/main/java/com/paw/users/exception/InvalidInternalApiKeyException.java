package com.paw.users.exception;

public class InvalidInternalApiKeyException extends RuntimeException {

    public InvalidInternalApiKeyException(String message) {
        super(message);
    }
}
