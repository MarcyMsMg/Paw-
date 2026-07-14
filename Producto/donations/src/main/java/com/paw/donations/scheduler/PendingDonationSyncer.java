package com.paw.donations.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.paw.donations.enums.DonationStatus;
import com.paw.donations.model.Donation;
import com.paw.donations.repository.DonationRepository;
import com.paw.donations.service.DonationService;
import com.paw.donations.service.MercadoPagoService;

import lombok.RequiredArgsConstructor;

// Reconciliador: cada cierto tiempo revisa las donaciones PENDING y le pregunta a
// MercadoPago si ya tienen un pago (buscando por la referencia de la donación). Si lo
// encuentra, lo procesa como el webhook. Así la donación se aprueba sola sin depender
// de que MercadoPago alcance el webhook ni de que el usuario vuelva a la página.
@Component
@RequiredArgsConstructor
public class PendingDonationSyncer {

    private final DonationRepository donationRepository;
    private final MercadoPagoService mercadoPagoService;
    private final DonationService donationService;

    @Scheduled(fixedDelayString = "${donations.sync-check-ms:20000}")
    public void syncPending() {
        List<Donation> pendientes = donationRepository.findByStatus(DonationStatus.PENDING);
        for (Donation donation : pendientes) {
            String paymentId = mercadoPagoService.findPaymentIdByExternalReference(donation.getId().toString());
            if (paymentId == null) {
                continue;
            }
            try {
                // handlePaymentNotification es idempotente: si entremedio llegó el webhook
                // o el sync del frontend, no pasa nada.
                donationService.handlePaymentNotification(paymentId);
            } catch (RuntimeException ex) {
                // Best-effort: si falla una, seguimos con las demás en el próximo ciclo.
            }
        }
    }
}
