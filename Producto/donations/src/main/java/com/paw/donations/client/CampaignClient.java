package com.paw.donations.client;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.paw.donations.client.dto.CampaignApiResponse;
import com.paw.donations.client.dto.CampaignData;
import com.paw.donations.exception.ResourceNotFoundException;
import com.paw.donations.exception.ServiceUnavailableException;

import lombok.RequiredArgsConstructor;

// Comunicación con el microservicio de Campañas: validar la campaña y, cuando una
// donación se aprueba, sumar el monto al recaudado (actualizarBarraProgreso).
@Component
@RequiredArgsConstructor
public class CampaignClient {

    private final RestClient campaignsRestClient;

    // Clave compartida para autenticar las llamadas internas a /raise y /lower.
    @Value("${services.campaigns.internal-api-key:}")
    private String internalApiKey;

    public CampaignData getCampaign(UUID campaignId) {
        try {
            CampaignApiResponse response = campaignsRestClient.get()
                    .uri("/campaigns/{id}", campaignId)
                    .retrieve()
                    .body(CampaignApiResponse.class);

            if (response == null || response.data() == null) {
                throw new ResourceNotFoundException("No se pudo obtener la campaña");
            }
            return response.data();

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("La campaña indicada no existe");
        } catch (ResourceAccessException ex) {
            throw new ServiceUnavailableException(
                    "El servicio de campañas no está disponible. Inténtalo más tarde.");
        }
    }

    public void addRaisedAmount(UUID campaignId, BigDecimal amount) {
        try {
            campaignsRestClient.patch()
                    .uri("/campaigns/{id}/raise", campaignId)
                    .header("X-Internal-Api-Key", internalApiKey)
                    .body(Map.of("amount", amount))
                    .retrieve()
                    .toBodilessEntity();

        } catch (ResourceAccessException ex) {
            throw new ServiceUnavailableException(
                    "No se pudo actualizar el monto recaudado de la campaña.");
        }
    }

    // Descuenta del recaudado de la campaña (al reembolsar/contracargar una donación).
    public void lowerRaisedAmount(UUID campaignId, BigDecimal amount) {
        try {
            campaignsRestClient.patch()
                    .uri("/campaigns/{id}/lower", campaignId)
                    .header("X-Internal-Api-Key", internalApiKey)
                    .body(Map.of("amount", amount))
                    .retrieve()
                    .toBodilessEntity();

        } catch (ResourceAccessException ex) {
            throw new ServiceUnavailableException(
                    "No se pudo descontar del monto recaudado de la campaña.");
        }
    }
}
