package com.paw.campaigns.controller;

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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.paw.campaigns.dto.response.CampaignResponse;
import com.paw.campaigns.enums.CampaignStatus;
import com.paw.campaigns.exception.ResourceNotFoundException;
import com.paw.campaigns.security.JwtService;
import com.paw.campaigns.service.CampaignService;

/**
 * Pruebas de INTEGRACION del microservicio de Campañas a nivel de capa web.
 *
 * Con @WebMvcTest se levanta solo la porción web del contexto de Spring (ruteo,
 * serializacion/deserializacion JSON, validacion @Valid y manejo global de errores),
 * con el servicio de negocio simulado (@MockitoBean). Se ejercita el controlador de
 * extremo a extremo a traves de MockMvc, sin necesidad de base de datos ni de levantar
 * el microservicio.
 */
@WebMvcTest(CampaignController.class)
@AutoConfigureMockMvc(addFilters = false)
class CampaignControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampaignService campaignService;

    // El filtro JWT es un bean de la capa web; se simula para el slice @WebMvcTest.
    @MockitoBean
    private JwtService jwtService;

    @Test
    void getCampanas_devuelveOkConLaLista() throws Exception {
        when(campaignService.findAllActive()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Rescate de Firulais"));
    }

    @Test
    void getCampanaPorIdInexistente_devuelve404() throws Exception {
        when(campaignService.findById(any(UUID.class)))
                .thenThrow(new ResourceNotFoundException("Campaña no encontrada"));

        mockMvc.perform(get("/api/campaigns/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void crearCampana_conDatosInvalidos_devuelve400YNoLlamaAlServicio() throws Exception {
        // Falta la mayoria de los campos obligatorios y el titulo es demasiado corto.
        String body = "{\"title\":\"a\"}";

        mockMvc.perform(post("/api/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(campaignService, never()).create(any());
    }

    @Test
    void crearCampana_conDatosValidos_devuelveOkYLlamaAlServicio() throws Exception {
        when(campaignService.create(any())).thenReturn(sampleResponse());

        String body = """
                {
                  "ngoId": "11111111-1111-1111-1111-111111111111",
                  "title": "Rescate de Firulais",
                  "description": "Descripcion larga de la campana de prueba para superar la validacion minima de longitud.",
                  "category": "Emergencia",
                  "goalAmount": 1000000,
                  "startDate": "2026-07-01",
                  "endDate": "2026-08-01"
                }
                """;

        mockMvc.perform(post("/api/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Rescate de Firulais"));

        verify(campaignService).create(any());
    }

    private CampaignResponse sampleResponse() {
        return new CampaignResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Rescate de Firulais",
                "Descripcion larga de la campana de prueba para superar la validacion minima de longitud.",
                null,
                null,
                "Emergencia",
                new BigDecimal("1000000"),
                BigDecimal.ZERO,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                CampaignStatus.ACTIVE,
                Instant.now());
    }
}
