package com.paw.adoptions.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.paw.adoptions.domain.AnimalStatus;
import com.paw.adoptions.dto.AnimalResponse;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.facade.AdoptionFacade;
import com.paw.adoptions.security.JwtService;
import com.paw.adoptions.service.AdoptionApplicationService;
import com.paw.adoptions.service.AdoptionFormService;
import com.paw.adoptions.service.AnimalService;

@WebMvcTest({AnimalController.class, AdoptionApplicationController.class, NgoAnimalController.class})
@AutoConfigureMockMvc(addFilters = false)
class AdoptionsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @MockitoBean
    private AdoptionFormService formService;

    @MockitoBean
    private AdoptionFacade adoptionFacade;

    @MockitoBean
    private AdoptionApplicationService applicationService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void listarAnimalesPublicos_conFiltros_devuelveOk() throws Exception {
        // Arrange
        when(animalService.listPublic("Perro", null, AnimalStatus.AVAILABLE, null, null, null))
                .thenReturn(List.of(animalResponse()));

        // Act + Assert
        mockMvc.perform(get("/api/adoptions/animals")
                        .param("species", "Perro")
                        .param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Benito"));
    }

    @Test
    void obtenerAnimalInexistente_devuelve404() throws Exception {
        // Arrange
        when(animalService.getPublic(any(UUID.class))).thenThrow(ApiException.notFound("Animal not found"));

        // Act + Assert
        mockMvc.perform(get("/api/adoptions/animals/{animalId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postular_conDatosInvalidos_devuelve400YNoLlamaFacade() throws Exception {
        // Arrange
        String body = """
                {
                  "fullName": "A",
                  "email": "correo-invalido",
                  "phone": "123"
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/adoptions/animals/{animalId}/applications", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(adoptionFacade, never()).submitApplication(any(), any());
    }

    @Test
    void crearAnimalComoOng_conDatosValidos_devuelveCreated() throws Exception {
        // Arrange
        when(animalService.create(any())).thenReturn(animalResponse());
        String body = """
                {
                  "name": "Benito",
                  "species": "Perro",
                  "age": "6 meses",
                  "sex": "Macho",
                  "size": "Pequeno",
                  "location": "Santiago",
                  "healthStatus": "Sano",
                  "description": "Benito es un cachorro tranquilo y sociable que busca una familia responsable.",
                  "adoptionRequirements": "Compromiso y hogar seguro",
                  "photoUrls": ["https://img.test/benito.jpg"],
                  "status": "AVAILABLE"
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/adoptions/ngo/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Benito"));
    }

    private AnimalResponse animalResponse() {
        Instant now = Instant.now();
        return new AnimalResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Benito",
                "Perro",
                "6 meses",
                "Macho",
                "Pequeno",
                "Santiago",
                "Sano",
                "Benito es un cachorro tranquilo y sociable que busca una familia responsable.",
                "Compromiso y hogar seguro",
                null,
                List.of("https://img.test/benito.jpg"),
                AnimalStatus.AVAILABLE,
                true,
                now,
                now
        );
    }
}
