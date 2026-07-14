package com.paw.campaigns.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

// Lo usará el futuro microservicio de Donaciones para sumar al monto recaudado
// (actualizarBarraProgreso del diagrama).
public record RaiseAmountRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount
) {
}
