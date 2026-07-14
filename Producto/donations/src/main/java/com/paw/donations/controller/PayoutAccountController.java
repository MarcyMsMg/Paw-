package com.paw.donations.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.donations.dto.request.UpsertPayoutAccountRequest;
import com.paw.donations.dto.response.ApiResponse;
import com.paw.donations.dto.response.PayoutAccountResponse;
import com.paw.donations.service.PayoutAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Datos de transferencia de una ONG. La ONG los gestiona desde su perfil.
@RestController
@RequestMapping("/api/payout-accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
public class PayoutAccountController {

    private final PayoutAccountService payoutAccountService;

    @GetMapping("/{ngoId}")
    public ApiResponse<PayoutAccountResponse> getByNgo(@PathVariable UUID ngoId) {
        return new ApiResponse<>(true, "Datos de transferencia encontrados", payoutAccountService.getByNgo(ngoId));
    }

    @PutMapping("/{ngoId}")
    public ApiResponse<PayoutAccountResponse> upsert(
            @PathVariable UUID ngoId,
            @Valid @RequestBody UpsertPayoutAccountRequest request
    ) {
        return new ApiResponse<>(true, "Datos de transferencia guardados", payoutAccountService.upsert(ngoId, request));
    }
}
