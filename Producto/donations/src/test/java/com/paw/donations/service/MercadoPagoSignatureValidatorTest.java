package com.paw.donations.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MercadoPagoSignatureValidatorTest {

    private static String hmacSha256Hex(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Test
    void isValid_debeAceptarCualquierFirma_cuandoNoHaySecretoConfigurado() {
        // Arrange: sin clave configurada, la validacion queda desactivada (modo desarrollo)
        MercadoPagoSignatureValidator validator = new MercadoPagoSignatureValidator();
        ReflectionTestUtils.setField(validator, "secret", "");

        // Act
        boolean result = validator.isValid("ts=123,v1=firma-cualquiera", "req-1", "data-1");

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_debeRechazar_cuandoNoVieneElHeaderDeFirma() {
        // Arrange
        MercadoPagoSignatureValidator validator = new MercadoPagoSignatureValidator();
        ReflectionTestUtils.setField(validator, "secret", "mi-clave-secreta");

        // Act
        boolean result = validator.isValid(null, "req-1", "data-1");

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_debeAceptar_cuandoLaFirmaCoincideConElManifestCalculado() throws Exception {
        // Arrange
        MercadoPagoSignatureValidator validator = new MercadoPagoSignatureValidator();
        String secret = "mi-clave-secreta";
        ReflectionTestUtils.setField(validator, "secret", secret);

        String dataId = "123456";
        String requestId = "req-abc";
        String ts = "1700000000";
        String manifest = "id:" + dataId.toLowerCase() + ";request-id:" + requestId + ";ts:" + ts + ";";
        String v1 = hmacSha256Hex(manifest, secret);
        String xSignature = "ts=" + ts + ",v1=" + v1;

        // Act
        boolean result = validator.isValid(xSignature, requestId, dataId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_debeRechazar_cuandoLaFirmaNoCoincide() {
        // Arrange
        MercadoPagoSignatureValidator validator = new MercadoPagoSignatureValidator();
        ReflectionTestUtils.setField(validator, "secret", "mi-clave-secreta");

        // Act
        boolean result = validator.isValid("ts=1700000000,v1=firma-invalida", "req-abc", "123456");

        // Assert
        assertFalse(result);
    }
}
