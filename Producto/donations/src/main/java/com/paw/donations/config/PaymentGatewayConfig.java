package com.paw.donations.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

// Inicializa el SDK de MercadoPago con el Access Token al arrancar el servicio.
// El token de MercadoPago es global (estático) en el SDK; lo seteamos una sola vez.
@Configuration
@RequiredArgsConstructor
public class PaymentGatewayConfig {

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @PostConstruct
    public void init() {
        // Si el token está vacío el servicio igual arranca; las llamadas de pago
        // fallarán recién cuando alguien intente donar.
        com.mercadopago.MercadoPagoConfig.setAccessToken(accessToken);
    }
}
