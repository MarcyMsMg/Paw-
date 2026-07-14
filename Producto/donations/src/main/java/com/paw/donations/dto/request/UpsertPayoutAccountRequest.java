package com.paw.donations.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpsertPayoutAccountRequest(
        @NotBlank String holderName,
        @NotBlank String rut,
        @NotBlank String bankName,
        @NotBlank String accountType,
        @NotBlank String accountNumber,
        String email
) {
}
