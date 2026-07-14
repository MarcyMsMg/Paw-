package com.paw.campaigns.client.dto;

// Refleja el envoltorio ApiResponse<T> que devuelve el microservicio de Usuarios,
// para poder deserializar su respuesta { success, message, data }.
public record NgoApiResponse(
        boolean success,
        String message,
        NgoData data
) {
}
