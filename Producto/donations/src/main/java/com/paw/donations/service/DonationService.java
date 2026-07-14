package com.paw.donations.service;

import java.util.List;
import java.util.UUID;

import com.paw.donations.dto.response.DonationStatsResponse;
import com.paw.donations.dto.request.CreateDonationRequest;
import com.paw.donations.dto.response.CheckoutResponse;
import com.paw.donations.dto.response.CampaignDonationSummaryResponse;
import com.paw.donations.dto.response.DonationResponse;
import com.paw.donations.dto.response.ReceiptResponse;

public interface DonationService {

    CheckoutResponse create(CreateDonationRequest request);

    DonationResponse findById(UUID id);

    // Comprobante de una donación APROBADA.
    ReceiptResponse getReceipt(UUID id);

    List<DonationResponse> findByDonor(UUID donorId);

    List<DonationResponse> findByCampaign(UUID campaignId);

    CampaignDonationSummaryResponse campaignSummary(UUID campaignId);

    List<DonationResponse> findByNgo(UUID ngoId);

    // Procesa la notificación (webhook) de MercadoPago para un pago.
    void handlePaymentNotification(String paymentId);

    DonationStatsResponse statsForPerson(UUID donorId);

    DonationStatsResponse statsForNgo(UUID ngoId);

    DonationStatsResponse statsForAdmin();
}
