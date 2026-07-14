package com.paw.donations.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.paw.donations.client.dto.UserApiResponse;
import com.paw.donations.client.dto.UserData;

import lombok.RequiredArgsConstructor;

// Consulta usuarios en Usuarios para enriquecer el comprobante (nombre del donante,
// nombre de la ONG). Usa GET /api/ngos/{id}, que es público y devuelve cualquier usuario.
// Es best-effort: si falla (servicio caído, no existe), devuelve null y el comprobante
// se arma igual sin ese dato.
@Component
@RequiredArgsConstructor
public class UsersClient {

    private final RestClient usersRestClient;

    public UserData getUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        try {
            UserApiResponse response = usersRestClient.get()
                    .uri("/ngos/{id}", userId)
                    .retrieve()
                    .body(UserApiResponse.class);
            return response != null ? response.data() : null;
        } catch (RestClientException ex) {
            return null;
        }
    }
}
