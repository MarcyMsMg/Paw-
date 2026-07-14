package com.paw.donations.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paw.donations.service.DonationService;
import com.paw.donations.service.MercadoPagoSignatureValidator;

import lombok.RequiredArgsConstructor;

// Recibe las notificaciones (webhook) de MercadoPago. MercadoPago manda el aviso en
// varios formatos según la versión: por query (?type=payment&data.id=..., o el viejo
// ?topic=payment&id=...) y/o en el body. Extraemos el id del pago y lo procesamos.
//
// Antes de procesar, validamos la firma (x-signature) para asegurar que el aviso es
// legítimo. En local, si no hay clave secreta configurada, la validación se omite.
//
// IMPORTANTE: en local MercadoPago NO puede llegar a localhost. Para recibir webhooks
// reales hay que exponer este endpoint con ngrok y poner esa URL https en
// mercadopago.notification-url. Mientras tanto se puede simular con una llamada manual.
@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class WebhookController {

    private final DonationService donationService;
    private final MercadoPagoSignatureValidator signatureValidator;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader(name = "x-signature", required = false) String xSignature,
            @RequestHeader(name = "x-request-id", required = false) String xRequestId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String topic,
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestParam(required = false) String id,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        // La firma se calcula sobre el data.id que viene en la query.
        if (!signatureValidator.isValid(xSignature, xRequestId, dataId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String notificationType = (type != null) ? type : topic;
        String paymentId = (dataId != null) ? dataId : id;

        // Si no vino por query, intentamos sacarlo del body { type, data: { id } }.
        if (body != null) {
            if (notificationType == null && body.get("type") != null) {
                notificationType = body.get("type").toString();
            }
            if (paymentId == null && body.get("data") instanceof Map<?, ?> data && data.get("id") != null) {
                paymentId = data.get("id").toString();
            }
        }

        // Solo nos interesan las notificaciones de pagos.
        boolean isPayment = notificationType == null || notificationType.contains("payment");
        if (paymentId != null && isPayment) {
            donationService.handlePaymentNotification(paymentId);
        }

        // MercadoPago espera 200/201; si no, reintenta.
        return ResponseEntity.ok().build();
    }
}
