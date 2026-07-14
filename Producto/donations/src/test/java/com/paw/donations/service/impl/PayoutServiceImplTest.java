package com.paw.donations.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paw.donations.client.CampaignClient;
import com.paw.donations.client.UsersClient;
import com.paw.donations.client.dto.CampaignData;
import com.paw.donations.dto.request.CreatePayoutRequest;
import com.paw.donations.dto.response.NgoBalanceResponse;
import com.paw.donations.dto.response.PayoutResponse;
import com.paw.donations.enums.DonationStatus;
import com.paw.donations.enums.DonationType;
import com.paw.donations.model.Donation;
import com.paw.donations.model.Payout;
import com.paw.donations.repository.DonationRepository;
import com.paw.donations.repository.PayoutRepository;

@ExtendWith(MockitoExtension.class)
class PayoutServiceImplTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private CampaignClient campaignClient;

    @Mock
    private UsersClient usersClient;

    private PayoutServiceImpl payoutService;
    private UUID ngoId;
    private UUID campaignId;

    @BeforeEach
    void setUp() {
        payoutService = new PayoutServiceImpl(donationRepository, payoutRepository, campaignClient, usersClient);
        ngoId = UUID.randomUUID();
        campaignId = UUID.randomUUID();
    }

    private Donation approvedDonation(BigDecimal paidAmount, BigDecimal netAmount) {
        return Donation.builder()
                .id(UUID.randomUUID())
                .donorId(UUID.randomUUID())
                .campaignId(campaignId)
                .ngoId(ngoId)
                .type(DonationType.CAMPAIGN)
                .amount(paidAmount)
                .paidAmount(paidAmount)
                .netAmount(netAmount)
                .status(DonationStatus.APPROVED)
                .build();
    }

    @Test
    void getBalance_debeContarSoloDonacionesDeCampanasCompletadas() {
        // Arrange
        Donation donacion = approvedDonation(new BigDecimal("10000"), new BigDecimal("9651"));
        when(donationRepository.findByNgoId(ngoId)).thenReturn(List.of(donacion));
        when(campaignClient.getCampaign(campaignId)).thenReturn(new CampaignData(campaignId, ngoId, "Ayuda a Firulais", "COMPLETED"));
        when(payoutRepository.findByNgoId(ngoId)).thenReturn(List.of());
        when(usersClient.getUser(ngoId)).thenReturn(null);

        // Act
        NgoBalanceResponse balance = payoutService.getBalance(ngoId);

        // Assert: usa el monto NETO (9651), no el bruto (10000)
        assertEquals(new BigDecimal("9651"), balance.totalDonated());
        assertEquals(new BigDecimal("9651"), balance.balanceOwed());
    }

    @Test
    void getBalance_debeIgnorarDonaciones_deCampanasQueAunNoEstanCompletadas() {
        // Arrange
        Donation donacion = approvedDonation(new BigDecimal("10000"), new BigDecimal("9651"));
        when(donationRepository.findByNgoId(ngoId)).thenReturn(List.of(donacion));
        when(campaignClient.getCampaign(campaignId)).thenReturn(new CampaignData(campaignId, ngoId, "Ayuda a Firulais", "ACTIVE"));
        when(payoutRepository.findByNgoId(ngoId)).thenReturn(List.of());
        when(usersClient.getUser(ngoId)).thenReturn(null);

        // Act
        NgoBalanceResponse balance = payoutService.getBalance(ngoId);

        // Assert: la campaña sigue ACTIVE, todavía no libera fondos
        assertEquals(BigDecimal.ZERO, balance.totalDonated());
    }

    @Test
    void createPayout_debeRegistrarTransferencia_cuandoElMontoNoSuperaElSaldo() {
        // Arrange
        Donation donacion = approvedDonation(new BigDecimal("10000"), new BigDecimal("9651"));
        when(donationRepository.findByNgoId(ngoId)).thenReturn(List.of(donacion));
        when(campaignClient.getCampaign(campaignId)).thenReturn(new CampaignData(campaignId, ngoId, "Ayuda a Firulais", "COMPLETED"));
        when(payoutRepository.findByNgoId(ngoId)).thenReturn(List.of());
        when(usersClient.getUser(ngoId)).thenReturn(null);
        when(payoutRepository.save(any(Payout.class))).thenAnswer(invocation -> {
            Payout payout = invocation.getArgument(0);
            payout.setId(UUID.randomUUID());
            return payout;
        });

        CreatePayoutRequest request = new CreatePayoutRequest(ngoId, new BigDecimal("5000"), "ref-001", "Transferencia julio");

        // Act
        PayoutResponse response = payoutService.createPayout(request);

        // Assert
        assertEquals(new BigDecimal("5000"), response.amount());
        verify(payoutRepository).save(any(Payout.class));
    }

    @Test
    void createPayout_debeLanzarExcepcion_cuandoElMontoSuperaElSaldoPendiente() {
        // Arrange
        Donation donacion = approvedDonation(new BigDecimal("10000"), new BigDecimal("9651"));
        when(donationRepository.findByNgoId(ngoId)).thenReturn(List.of(donacion));
        when(campaignClient.getCampaign(campaignId)).thenReturn(new CampaignData(campaignId, ngoId, "Ayuda a Firulais", "COMPLETED"));
        when(payoutRepository.findByNgoId(ngoId)).thenReturn(List.of());
        when(usersClient.getUser(ngoId)).thenReturn(null);

        // El saldo disponible es 9651 (neto); pedimos transferir mas que eso.
        CreatePayoutRequest request = new CreatePayoutRequest(ngoId, new BigDecimal("50000"), null, null);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> payoutService.createPayout(request));
        verify(payoutRepository, never()).save(any());
    }
}
