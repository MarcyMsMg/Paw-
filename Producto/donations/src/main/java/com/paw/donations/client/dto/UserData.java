package com.paw.donations.client.dto;

// Subconjunto de un usuario de Usuarios (sirve tanto para el donante como para la ONG).
public record UserData(
        String firstName,
        String lastName,
        String ngoName,
        String email
) {
}
