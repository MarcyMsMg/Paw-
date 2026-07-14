package com.paw.campaigns.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.paw.campaigns.client.dto.NgoApiResponse;
import com.paw.campaigns.client.dto.NgoData;
import com.paw.campaigns.exception.NgoNotEligibleException;
import com.paw.campaigns.exception.UsersServiceUnavailableException;

@Component
public class NgoClient {

    private final RestClient usersRestClient;

    public NgoClient(@Qualifier("usersRestClient") RestClient usersRestClient) {
        this.usersRestClient = usersRestClient;
    }

    public NgoData getNgo(UUID ngoId) {
        try {
            NgoApiResponse response = usersRestClient.get()
                    .uri("/ngos/{id}", ngoId)
                    .retrieve()
                    .body(NgoApiResponse.class);

            if (response == null || response.data() == null) {
                throw new NgoNotEligibleException("No se pudo obtener la informacion de la ONG");
            }
            return response.data();

        } catch (HttpClientErrorException.NotFound ex) {
            throw new NgoNotEligibleException("La ONG indicada no existe");
        } catch (ResourceAccessException ex) {
            throw new UsersServiceUnavailableException(
                    "El servicio de usuarios no esta disponible. Intentalo mas tarde.");
        }
    }
}