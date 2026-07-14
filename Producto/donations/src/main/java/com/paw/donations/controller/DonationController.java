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

import com.paw.donations.dto.request.CreateDonationRequest;
import com.paw.donations.dto.response.ApiResponse;
import com.paw.donations.dto.response.CheckoutResponse;
import com.paw.donations.dto.response.CampaignDonationSummaryResponse;
import com.paw.donations.dto.response.DonationResponse;
import com.paw.donations.dto.response.ReceiptResponse;
import com.paw.donations.service.DonationService;
import com.paw.donations.dto.response.DonationStatsResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    // Crea la donación (en PENDING) y devuelve la URL de checkout de MercadoPago.
    @PostMapping
    @PreAuthorize("hasRole('NATURAL_PERSON')")
    public ApiResponse<CheckoutResponse> create(
            @Valid @RequestBody CreateDonationRequest request
    ) {
        return new ApiResponse<>(
                true,
                "Donación iniciada. Redirige al usuario a la URL de pago.",
                donationService.create(request)
        );
    }

    // GET /api/donations?donorId=...   -> donaciones de una persona (su historial)
    // GET /api/donations?campaignId=... -> donaciones de una campaña
    // GET /api/donations?ngoId=...      -> donaciones recibidas por una ONG
    @GetMapping
    public ApiResponse<List<DonationResponse>> find(
            @RequestParam(required = false) UUID donorId,
            @RequestParam(required = false) UUID campaignId,
            @RequestParam(required = false) UUID ngoId
    ) {
        List<DonationResponse> data;
        if (donorId != null) {
            data = donationService.findByDonor(donorId);
        } else if (campaignId != null) {
            data = donationService.findByCampaign(campaignId);
        } else if (ngoId != null) {
            data = donationService.findByNgo(ngoId);
        } else {
            data = List.of();
        }
        return new ApiResponse<>(true, "Donaciones encontradas", data);
    }

    @GetMapping("/campaigns/{campaignId}/summary")
    public ApiResponse<CampaignDonationSummaryResponse> campaignSummary(@PathVariable UUID campaignId) {
        return new ApiResponse<>(true, "Resumen de donaciones encontrado", donationService.campaignSummary(campaignId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DonationResponse> findById(@PathVariable UUID id) {
        return new ApiResponse<>(true, "Donación encontrada", donationService.findById(id));
    }

    // Lo llama el frontend al volver de MercadoPago (la back_url trae el payment_id).
    // Procesa el pago igual que el webhook, así la donación se aprueba sin depender de
    // que MercadoPago alcance el webhook (clave en local sin ngrok). Es idempotente.
    @PostMapping("/sync-payment")
    public ApiResponse<Void> syncPayment(@RequestParam String paymentId) {
        donationService.handlePaymentNotification(paymentId);
        return new ApiResponse<>(true, "Pago sincronizado", null);
    }

    // Comprobante de una donación aprobada.
    @GetMapping("/{id}/receipt")
    public ApiResponse<ReceiptResponse> getReceipt(@PathVariable UUID id) {
        return new ApiResponse<>(true, "Comprobante de la donación", donationService.getReceipt(id));
    }

        @GetMapping("/stats/person")
    @PreAuthorize("hasRole('NATURAL_PERSON')")
    public ApiResponse<DonationStatsResponse> statsForPerson(@RequestParam UUID donorId) {
        return new ApiResponse<>(
                true,
                "Estadísticas de donaciones de la persona encontradas",
                donationService.statsForPerson(donorId)
        );
    }

    @GetMapping("/stats/ngo")
    @PreAuthorize("hasRole('NGO')")
    public ApiResponse<DonationStatsResponse> statsForNgo(@RequestParam UUID ngoId) {
        return new ApiResponse<>(
                true,
                "Estadísticas de donaciones de la ONG encontradas",
                donationService.statsForNgo(ngoId)
        );
    }

    @GetMapping("/stats/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DonationStatsResponse> statsForAdmin() {
        return new ApiResponse<>(
                true,
                "Estadísticas globales de donaciones encontradas",
                donationService.statsForAdmin()
        );
    }

     }
