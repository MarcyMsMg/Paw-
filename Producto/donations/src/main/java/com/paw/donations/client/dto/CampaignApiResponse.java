package com.paw.donations.client.dto;

// Refleja el envoltorio ApiResponse<T> que devuelve el microservicio de Campañas.
public record CampaignApiResponse(
        boolean success,
        String message,
        CampaignData data
) {
}
