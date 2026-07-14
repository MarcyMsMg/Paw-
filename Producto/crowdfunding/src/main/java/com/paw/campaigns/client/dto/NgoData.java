package com.paw.campaigns.client.dto;

import java.util.UUID;

// Subconjunto de los datos de la ONG que nos interesan desde este servicio.
// El microservicio de Usuarios devuelve más campos; Jackson ignora los que no usamos.
public record NgoData(
        UUID id,
        String ngoName,
        String status
) {
}
