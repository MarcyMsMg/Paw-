package com.paw.donations.service;

import java.util.List;
import java.util.UUID;

import com.paw.donations.dto.request.CreatePayoutRequest;
import com.paw.donations.dto.response.NgoBalanceResponse;
import com.paw.donations.dto.response.PayoutResponse;

public interface PayoutService {

    // Saldo pendiente de pago para todas las ONGs que tienen donaciones.
    List<NgoBalanceResponse> getBalances();

    // Saldo pendiente de pago para una ONG puntual.
    NgoBalanceResponse getBalance(UUID ngoId);

    // El admin registra una transferencia ya hecha a una ONG.
    PayoutResponse createPayout(CreatePayoutRequest request);

    // Historial de transferencias de una ONG.
    List<PayoutResponse> findPayoutsByNgo(UUID ngoId);
}
