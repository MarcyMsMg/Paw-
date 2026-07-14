package com.paw.donations.client.dto;

import java.util.UUID;

// Subconjunto de la campaña que nos interesa desde Donaciones.
public record CampaignData(
        UUID id,
        UUID ngoId,
        String title,
        String status
) {
}
