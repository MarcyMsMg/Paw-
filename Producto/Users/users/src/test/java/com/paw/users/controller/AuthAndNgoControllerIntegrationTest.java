package com.paw.users.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.paw.users.dto.response.AuthResponse;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.exception.ResourceNotFoundException;
import com.paw.users.security.AuthTokenManager;
import com.paw.users.repository.UserRepository;
import com.paw.users.service.AdminNgoRequestService;
import com.paw.users.service.AuthService;
import com.paw.users.service.UserService;

@WebMvcTest({AuthController.class, NgosController.class, AdminNgoRequestController.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthAndNgoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AdminNgoRequestService adminNgoRequestService;

    @MockitoBean
    private AuthTokenManager authTokenManager;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void login_conCredencialesValidas_devuelveTokenYUsuario() throws Exception {
        // Arrange
        UserResponse user = userResponse(UserRole.NATURAL_PERSON, AccountStatus.ACTIVE);
        when(authService.login(any())).thenReturn(new AuthResponse("jwt-token", user));

        String body = """
                {
                  "email": "ana@test.com",
                  "password": "Password1"
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.email").value("user@test.com"));

        verify(authService).login(any());
    }

    @Test
    void registrarPersona_conDatosInvalidos_devuelve400YNoLlamaServicio() throws Exception {
        // Arrange
        String body = """
                {
                  "firstName": "A",
                  "email": "correo-invalido",
                  "password": "123"
                }
                """;

        // Act + Assert
        mockMvc.perform(post("/api/auth/register/natural-person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(authService, never()).registerNaturalPerson(any());
    }

    @Test
    void listarOngs_devuelveSoloDatosPublicos() throws Exception {
        // Arrange
        when(userService.findAllActiveNgos()).thenReturn(List.of(userResponse(UserRole.NGO, AccountStatus.ACTIVE)));

        // Act + Assert
        mockMvc.perform(get("/api/ngos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].ngoName").value("Fundacion Test"));
    }

    @Test
    void buscarOngInexistente_devuelve404() throws Exception {
        // Arrange
        when(userService.findActiveNgoById(any(UUID.class))).thenThrow(new ResourceNotFoundException("ONG no encontrada"));

        // Act + Assert
        mockMvc.perform(get("/api/ngos/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    private UserResponse userResponse(UserRole role, AccountStatus status) {
        return new UserResponse(
                UUID.randomUUID(),
                "user@test.com",
                role == UserRole.NATURAL_PERSON ? "Ana" : null,
                role == UserRole.NATURAL_PERSON ? "Perez" : null,
                role == UserRole.NGO ? "Fundacion Test" : null,
                "https://img.test/logo.png",
                "Descripcion publica de prueba para una fundacion",
                "https://img.test/cover.png",
                "Santiago, Chile",
                2020,
                12,
                4,
                role,
                status,
                LocalDateTime.now()
        );
    }
}

