package com.paw.donations.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResultsResourcesPage;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.paw.donations.exception.PaymentGatewayException;

// Encapsula toda la interacción con MercadoPago (Checkout Pro):
//  - crear la preferencia de pago y obtener la URL de checkout
//  - consultar un pago por su id (lo usamos al recibir el webhook)
// Los demás componentes no conocen el SDK: trabajan con estos records.
@Service
public class MercadoPagoService {

    @Value("${mercadopago.currency:CLP}")
    private String currency;

    @Value("${mercadopago.back-url.success:}")
    private String successUrl;

    @Value("${mercadopago.back-url.failure:}")
    private String failureUrl;

    @Value("${mercadopago.back-url.pending:}")
    private String pendingUrl;

    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    // "approved" = MercadoPago redirige solo al sitio tras un pago aprobado.
    // "none" para desactivar (si localhost diera problemas con auto_return).
    @Value("${mercadopago.auto-return:approved}")
    private String autoReturn;

    public record PreferenceResult(String preferenceId, String checkoutUrl) {
    }

    // amount = transaction_amount (BRUTO); netAmount = net_received_amount (lo que MP
    // acredita a nuestra cuenta tras descontar su comisión). netAmount puede ser null.
    public record PaymentResult(String status, String externalReference, String paymentType,
            BigDecimal amount, BigDecimal netAmount) {
    }

    public PreferenceResult createPreference(UUID donationId, String title, BigDecimal amount) {
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(donationId.toString())
                .title(title)
                .quantity(1)
                .unitPrice(amount)
                .currencyId(currency)
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();

        PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                .items(List.of(item))
                .backUrls(backUrls)
                .externalReference(donationId.toString());

        // Solo enviamos notification_url si está configurada (en local suele quedar vacía).
        if (StringUtils.hasText(notificationUrl)) {
            builder.notificationUrl(notificationUrl);
        }

        // auto_return hace que MercadoPago vuelva solo al sitio tras aprobar el pago.
        if (StringUtils.hasText(autoReturn) && !"none".equalsIgnoreCase(autoReturn)) {
            builder.autoReturn(autoReturn);
        }

        try {
            Preference preference = new PreferenceClient().create(builder.build());
            return new PreferenceResult(preference.getId(), preference.getInitPoint());
        } catch (MPApiException | MPException ex) {
            throw new PaymentGatewayException(
                    "No se pudo crear la preferencia de pago en MercadoPago: " + ex.getMessage());
        }
    }

    public PaymentResult getPayment(String paymentId) {
        try {
            Payment payment = new PaymentClient().get(Long.parseLong(paymentId));
            // net_received_amount vive dentro de transaction_details; puede no venir.
            BigDecimal netReceived = (payment.getTransactionDetails() != null)
                    ? payment.getTransactionDetails().getNetReceivedAmount()
                    : null;
            return new PaymentResult(
                    payment.getStatus(),
                    payment.getExternalReference(),
                    payment.getPaymentTypeId(),
                    payment.getTransactionAmount(),
                    netReceived
            );
        } catch (NumberFormatException ex) {
            throw new PaymentGatewayException("Id de pago de MercadoPago inválido: " + paymentId);
        } catch (MPApiException | MPException ex) {
            throw new PaymentGatewayException(
                    "No se pudo consultar el pago en MercadoPago: " + ex.getMessage());
        }
    }

    // Busca en MercadoPago el pago asociado a una donación (por external_reference) y
    // devuelve su id, o null si no hay ninguno. Best-effort: si falla, devuelve null.
    // Lo usa el reconciliador para aprobar donaciones sin depender del webhook.
    public String findPaymentIdByExternalReference(String externalReference) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("external_reference", externalReference);

            // offset y limit DEBEN ir seteados: si quedan null, el SDK falla al armar la URL.
            MPSearchRequest searchRequest = MPSearchRequest.builder()
                    .offset(0)
                    .limit(10)
                    .filters(filters)
                    .build();

            MPResultsResourcesPage<Payment> results = new PaymentClient().search(searchRequest);
            if (results == null || results.getResults() == null) {
                return null;
            }
            for (Payment payment : results.getResults()) {
                if (payment.getId() != null) {
                    return String.valueOf(payment.getId());
                }
            }
            return null;
        } catch (MPApiException | MPException ex) {
            return null;
        }
    }
}
