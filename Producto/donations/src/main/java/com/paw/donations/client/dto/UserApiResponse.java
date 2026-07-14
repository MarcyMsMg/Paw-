package com.paw.donations.client.dto;

// Envoltorio ApiResponse<T> de Usuarios.
public record UserApiResponse(
        boolean success,
        String message,
        UserData data
) {
}
