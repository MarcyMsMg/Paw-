package com.paw.donations.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paw.donations.dto.request.CreatePayoutRequest;
import com.paw.donations.dto.response.ApiResponse;
import com.paw.donations.dto.response.NgoBalanceResponse;
import com.paw.donations.dto.response.PayoutResponse;
import com.paw.donations.service.PayoutService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Endpoints del panel de administración para la liquidación de fondos a las ONGs.
@RestController
@RequestMapping("/api/admin/payouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPayoutController {

    private final PayoutService payoutService;

    // Saldo pendiente de todas las ONGs (cuánto se le debe a cada una).
    @GetMapping("/balances")
    public ApiResponse<List<NgoBalanceResponse>> getBalances() {
        return new ApiResponse<>(true, "Saldos pendientes por ONG", payoutService.getBalances());
    }

    // Saldo pendiente de una ONG puntual.
    @GetMapping("/balances/{ngoId}")
    public ApiResponse<NgoBalanceResponse> getBalance(@PathVariable UUID ngoId) {
        return new ApiResponse<>(true, "Saldo pendiente de la ONG", payoutService.getBalance(ngoId));
    }

    // Registrar una transferencia ya hecha a una ONG.
    @PostMapping
    public ApiResponse<PayoutResponse> create(@Valid @RequestBody CreatePayoutRequest request) {
        return new ApiResponse<>(true, "Transferencia registrada correctamente", payoutService.createPayout(request));
    }

    // Historial de transferencias de una ONG.
    @GetMapping
    public ApiResponse<List<PayoutResponse>> findByNgo(@RequestParam UUID ngoId) {
        return new ApiResponse<>(true, "Transferencias de la ONG", payoutService.findPayoutsByNgo(ngoId));
    }
}
