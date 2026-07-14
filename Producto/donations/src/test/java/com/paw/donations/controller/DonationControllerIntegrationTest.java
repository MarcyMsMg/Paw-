package com.paw.donations.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.paw.donations.dto.response.CheckoutResponse;
import com.paw.donations.dto.response.DonationResponse;
import com.paw.donations.enums.DonationStatus;
import com.paw.donations.enums.DonationType;
import com.paw.donations.exception.ResourceNotFoundException;
import com.paw.donations.service.DonationService;

@WebMvcTest(DonationController.class)
class DonationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DonationService donationService;

    @Test
    void listarPorCampana_devuelveDonaciones() throws Exception {
        // Arrange
        when(donationService.findByCampaign(any(UUID.class))).thenReturn(List.of(donationResponse()));

        // Act + Assert
        mockMvc.perform(get("/api/donations").param("campaignId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"));
    }

    @Test
    void crearDonacion_conDatosValidos_devuelveCheckout() throws Exception {
        // Arrange
        UUID donationId = UUID.randomUUID();
        when(donationService.create(any())).thenReturn(new CheckoutResponse(donationId, DonationStatus.PENDING, "https://checkout.test"));
        String body = """
                {
                  "donorId": "11111111-1111-1111-1111-111111111111",
                  "campaignId": "22222222-2222-2222-2222-222222222222",
                  "amount": 20000
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/donations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.checkoutUrl").value("https://checkout.test"));
    }

    @Test
    void crearDonacion_conMontoInvalido_devuelve400YNoLlamaServicio() throws Exception {
        // Arrange
        String body = """
                {
                  "donorId": "11111111-1111-1111-1111-111111111111",
                  "campaignId": "22222222-2222-2222-2222-222222222222",
                  "amount": 0
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/donations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(donationService, never()).create(any());
    }

    @Test
    void buscarDonacionInexistente_devuelve404() throws Exception {
        // Arrange
        when(donationService.findById(any(UUID.class))).thenThrow(new ResourceNotFoundException("Donacion no encontrada"));

        // Act + Assert
        mockMvc.perform(get("/api/donations/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    private DonationResponse donationResponse() {
        Instant now = Instant.now();
        return new DonationResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                DonationType.CAMPAIGN,
                new BigDecimal("20000"),
                new BigDecimal("20000"),
                DonationStatus.APPROVED,
                "mercadopago",
                "PAW-001",
                now,
                now
        );
    }
}
