package com.paw.donations.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paw.donations.client.CampaignClient;
import com.paw.donations.client.NotificationsClient;
import com.paw.donations.client.UsersClient;
import com.paw.donations.client.dto.CampaignData;
import com.paw.donations.dto.request.CreateDonationRequest;
import com.paw.donations.dto.response.CheckoutResponse;
import com.paw.donations.dto.response.ReceiptResponse;
import com.paw.donations.enums.DonationStatus;
import com.paw.donations.enums.DonationType;
import com.paw.donations.exception.InvalidDonationException;
import com.paw.donations.model.Donation;
import com.paw.donations.repository.DonationRepository;
import com.paw.donations.service.MercadoPagoService;
import com.paw.donations.service.MercadoPagoService.PaymentResult;
import com.paw.donations.service.MercadoPagoService.PreferenceResult;

@ExtendWith(MockitoExtension.class)
class DonationServiceImplTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private CampaignClient campaignClient;

    @Mock
    private UsersClient usersClient;

    @Mock
    private MercadoPagoService mercadoPagoService;

    @Mock
    private NotificationsClient notificationsClient;

    private DonationServiceImpl donationService;

    private UUID donorId;
    private UUID campaignId;
    private UUID ngoId;

    @BeforeEach
    void setUp() {
        donationService = new DonationServiceImpl(donationRepository, campaignClient, usersClient, mercadoPagoService, notificationsClient);
        donorId = UUID.randomUUID();
        campaignId = UUID.randomUUID();
        ngoId = UUID.randomUUID();
    }

    private Donation pendingDonation(UUID id, BigDecimal amount) {
        return Donation.builder()
                .id(id)
                .donorId(donorId)
                .campaignId(campaignId)
                .ngoId(ngoId)
                .type(DonationType.CAMPAIGN)
                .amount(amount)
                .status(DonationStatus.PENDING)
                .build();
    }

    @Test
    void create_debeCrearDonacionPendiente_cuandoLaCampanaEstaActiva() {
        // Arrange
        when(campaignClient.getCampaign(campaignId)).thenReturn(new CampaignData(campaignId, ngoId, "Ayuda a Firulais", "ACTIVE"));
        when(donationRepository.save(any(Donation.class))).thenAnswer(invocation -> {
            Donation donation = invocation.getArgument(0);
            donation.setId(UUID.randomUUID());
            return donation;
        });
        when(mercadoPagoService.createPreference(any(UUID.class), org.mockito.ArgumentMatchers.anyString(), eq(new BigDecimal("10000"))))
                .thenReturn(new PreferenceResult("pref-1", "https://mercadopago.cl/checkout/pref-1"));

        CreateDonationRequest request = new CreateDonationRequest(donorId, campaignId, new BigDecimal("10000"));

        // Act
        CheckoutResponse response = donationService.create(request);

        // Assert
        assertNotNull(response.donationId());
        assertEquals(DonationStatus.PENDING, response.status());
        assertEquals("https://mercadopago.cl/checkout/pref-1", response.checkoutUrl());
    }

    @Test
    void create_debeLanzarExcepcion_cuandoLaCampanaNoEstaActiva() {
        // Arrange
        when(campaignClient.getCampaign(campaignId)).thenReturn(new CampaignData(campaignId, ngoId, "Ayuda a Firulais", "COMPLETED"));
        CreateDonationRequest request = new CreateDonationRequest(donorId, campaignId, new BigDecimal("10000"));

        // Act + Assert
        assertThrows(InvalidDonationException.class, () -> donationService.create(request));
        verify(donationRepository, never()).save(any());
    }

    @Test
    void handlePaymentNotification_debeAprobarYSumarAlRecaudado_cuandoElPagoEsAprobado() {
        // Arrange
        UUID donationId = UUID.randomUUID();
        Donation donation = pendingDonation(donationId, new BigDecimal("10000"));
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));
        when(mercadoPagoService.getPayment("pay-1")).thenReturn(
                new PaymentResult("approved", donationId.toString(), "credit_card", new BigDecimal("10000"), new BigDecimal("9651"))
        );

        // Act
        donationService.handlePaymentNotification("pay-1");

        // Assert
        assertEquals(DonationStatus.APPROVED, donation.getStatus());
        assertEquals(new BigDecimal("10000"), donation.getPaidAmount());
        assertEquals(new BigDecimal("9651"), donation.getNetAmount());
        assertNotNull(donation.getReceiptNumber());
        verify(campaignClient, times(1)).addRaisedAmount(campaignId, new BigDecimal("10000"));
    }

    @Test
    void handlePaymentNotification_debeSerIdempotente_cuandoLaDonacionYaEstabaAprobada() {
        // Arrange: simula que el webhook de MercadoPago llega dos veces para el mismo pago.
        UUID donationId = UUID.randomUUID();
        Donation donation = pendingDonation(donationId, new BigDecimal("10000"));
        donation.setStatus(DonationStatus.APPROVED);
        donation.setPaidAmount(new BigDecimal("10000"));
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));
        when(mercadoPagoService.getPayment("pay-1")).thenReturn(
                new PaymentResult("approved", donationId.toString(), "credit_card", new BigDecimal("10000"), new BigDecimal("9651"))
        );

        // Act
        donationService.handlePaymentNotification("pay-1");

        // Assert: no se vuelve a sumar el monto a la campaÃƒÂ±a
        verify(campaignClient, never()).addRaisedAmount(any(), any());
    }

    @Test
    void handlePaymentNotification_debeDescontarDeLaCampana_cuandoElPagoEsReembolsado() {
        // Arrange
        UUID donationId = UUID.randomUUID();
        Donation donation = pendingDonation(donationId, new BigDecimal("10000"));
        donation.setStatus(DonationStatus.APPROVED);
        donation.setPaidAmount(new BigDecimal("10000"));
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));
        when(mercadoPagoService.getPayment("pay-1")).thenReturn(
                new PaymentResult("refunded", donationId.toString(), "credit_card", new BigDecimal("10000"), null)
        );

        // Act
        donationService.handlePaymentNotification("pay-1");

        // Assert
        assertEquals(DonationStatus.REFUNDED, donation.getStatus());
        verify(campaignClient, times(1)).lowerRaisedAmount(campaignId, new BigDecimal("10000"));
    }

    @Test
    void handlePaymentNotification_debeRechazarLaDonacion_cuandoElPagoEsRechazado() {
        // Arrange
        UUID donationId = UUID.randomUUID();
        Donation donation = pendingDonation(donationId, new BigDecimal("10000"));
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));
        when(mercadoPagoService.getPayment("pay-1")).thenReturn(
                new PaymentResult("rejected", donationId.toString(), null, null, null)
        );

        // Act
        donationService.handlePaymentNotification("pay-1");

        // Assert
        assertEquals(DonationStatus.REJECTED, donation.getStatus());
        verify(campaignClient, never()).addRaisedAmount(any(), any());
    }

    @Test
    void getReceipt_debeLanzarExcepcion_cuandoLaDonacionNoEstaAprobada() {
        // Arrange
        UUID donationId = UUID.randomUUID();
        Donation donation = pendingDonation(donationId, new BigDecimal("10000"));
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));

        // Act + Assert
        assertThrows(InvalidDonationException.class, () -> donationService.getReceipt(donationId));
    }

    @Test
    void getReceipt_debeDevolverComprobante_cuandoLaDonacionEstaAprobada() {
        // Arrange
        UUID donationId = UUID.randomUUID();
        Donation donation = pendingDonation(donationId, new BigDecimal("10000"));
        donation.setStatus(DonationStatus.APPROVED);
        donation.setPaidAmount(new BigDecimal("10000"));
        donation.setReceiptNumber("PAW-2026-ABCDEF12");
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));
        when(usersClient.getUser(any())).thenReturn(null);
        when(campaignClient.getCampaign(campaignId)).thenReturn(new CampaignData(campaignId, ngoId, "Ayuda a Firulais", "ACTIVE"));

        // Act
        ReceiptResponse response = donationService.getReceipt(donationId);

        // Assert
        assertEquals("PAW-2026-ABCDEF12", response.receiptNumber());
        assertEquals("Ayuda a Firulais", response.campaignTitle());
    }
    @Test
    void statsForPerson_debeCalcularTotalesPorMesYEstados() {
        // Arrange
        Donation approved = pendingDonation(UUID.randomUUID(), new BigDecimal("10000"));
        approved.setStatus(DonationStatus.APPROVED);
        approved.setPaidAmount(new BigDecimal("9000"));
        approved.setPaidAt(Instant.parse("2026-07-02T10:00:00Z"));
        Donation pending = pendingDonation(UUID.randomUUID(), new BigDecimal("5000"));
        Donation refunded = pendingDonation(UUID.randomUUID(), new BigDecimal("2000"));
        refunded.setStatus(DonationStatus.REFUNDED);
        when(donationRepository.findByDonorId(donorId)).thenReturn(List.of(approved, pending, refunded));

        // Act
        var stats = donationService.statsForPerson(donorId);

        // Assert
        assertEquals(new BigDecimal("9000"), stats.totalDonated());
        assertEquals(BigDecimal.ZERO, stats.totalRaised());
        assertEquals(1, stats.approvedDonations());
        assertEquals(1, stats.pendingDonations());
        assertEquals(1, stats.rejectedDonations());
        assertEquals(new BigDecimal("9000"), stats.donationsByMonth().get("2026-07"));
    }

    @Test
    void findByNgo_debeMapearDonacionesDeLaOng() {
        // Arrange
        Donation donation = pendingDonation(UUID.randomUUID(), new BigDecimal("3000"));
        when(donationRepository.findByNgoId(ngoId)).thenReturn(List.of(donation));

        // Act
        var response = donationService.findByNgo(ngoId);

        // Assert
        assertEquals(1, response.size());
        assertEquals(ngoId, response.getFirst().ngoId());
        assertEquals(DonationStatus.PENDING, response.getFirst().status());
    }

    @Test
    void handlePaymentNotification_debeMarcarContracargoYDescontar_cuandoPagoTieneChargedBack() {
        // Arrange
        UUID donationId = UUID.randomUUID();
        Donation donation = pendingDonation(donationId, new BigDecimal("10000"));
        donation.setStatus(DonationStatus.APPROVED);
        donation.setPaidAmount(new BigDecimal("10000"));
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));
        when(mercadoPagoService.getPayment("pay-2")).thenReturn(
                new PaymentResult("charged_back", donationId.toString(), "credit_card", new BigDecimal("10000"), null)
        );

        // Act
        donationService.handlePaymentNotification("pay-2");

        // Assert
        assertEquals(DonationStatus.CHARGED_BACK, donation.getStatus());
        verify(campaignClient).lowerRaisedAmount(campaignId, new BigDecimal("10000"));
    }
}
