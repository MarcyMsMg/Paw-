package com.paw.donations.service;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// Valida la firma (header x-signature) que MercadoPago envía en los webhooks, para
// asegurar que la notificación viene realmente de MercadoPago.
//
// MercadoPago firma un "manifest":  id:<data.id>;request-id:<x-request-id>;ts:<ts>;
// con HMAC-SHA256 usando la "clave secreta" del webhook (panel de MercadoPago).
// El header x-signature viene como:  ts=<timestamp>,v1=<hash-hex>
//
// Si no hay clave secreta configurada (mercadopago.webhook-secret vacío), se omite
// la validación — útil en desarrollo. En producción hay que configurarla.
@Component
public class MercadoPagoSignatureValidator {

    @Value("${mercadopago.webhook-secret:}")
    private String secret;

    public boolean isValid(String xSignature, String xRequestId, String dataId) {
        if (!StringUtils.hasText(secret)) {
            return true; // validación desactivada (sin clave configurada)
        }
        if (!StringUtils.hasText(xSignature)) {
            return false;
        }

        String ts = null;
        String v1 = null;
        for (String part : xSignature.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();
                if ("ts".equals(key)) {
                    ts = value;
                } else if ("v1".equals(key)) {
                    v1 = value;
                }
            }
        }
        if (ts == null || v1 == null) {
            return false;
        }

        String id = (dataId == null) ? "" : dataId.toLowerCase();
        String requestId = (xRequestId == null) ? "" : xRequestId;
        String manifest = "id:" + id + ";request-id:" + requestId + ";ts:" + ts + ";";

        String expected = hmacSha256Hex(manifest, secret);
        return expected.equalsIgnoreCase(v1);
    }

    private String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Error calculando la firma HMAC", ex);
        }
    }
}
