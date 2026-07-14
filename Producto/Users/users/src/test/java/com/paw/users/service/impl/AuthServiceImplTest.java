package com.paw.users.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.paw.users.client.NotificationsClient;
import com.paw.users.dto.request.NaturalPersonRegisterRequest;
import com.paw.users.dto.request.NgoRegisterRequest;
import com.paw.users.dto.response.NgoRegistrationRequestResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.exception.InvalidRequestException;
import com.paw.users.model.NgoRegistrationRequest;
import com.paw.users.model.User;
import com.paw.users.repository.NgoRegistrationRequestRepository;
import com.paw.users.repository.UserRepository;
import com.paw.users.security.AuthTokenManager;

/**
 * Complementa a {@link com.paw.users.AuthServiceTest}: cubre la solicitud de registro de ONG
 * (no probada allí) y las validaciones nuevas de contraseña con espacios y año de fundación futuro.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NgoRegistrationRequestRepository ngoRegistrationRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenManager authTokenManager;

    @Mock
    private NotificationsClient notificationsClient;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, ngoRegistrationRequestRepository, passwordEncoder, authTokenManager, notificationsClient);
    }

    @Test
    void registerNaturalPerson_debeNormalizarElEmail_cuandoTieneEspaciosYMayusculas() {
        // Arrange
        NaturalPersonRegisterRequest request = new NaturalPersonRegisterRequest(
                "Maria", "Perez", "  Maria.Perez@Example.COM  ", "Abcd1234", null
        );
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Abcd1234")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authTokenManager.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        authService.registerNaturalPerson(request);

        // Assert
        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("maria.perez@example.com", savedUser.getValue().getEmail());
    }

    @Test
    void registerNaturalPerson_debeLanzarExcepcion_cuandoPasswordTieneEspaciosAlBorde() {
        // Arrange
        NaturalPersonRegisterRequest request = new NaturalPersonRegisterRequest(
                "Maria", "Perez", "maria@example.com", " Abcd1234", null
        );
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        // Act + Assert
        assertThrows(InvalidRequestException.class, () -> authService.registerNaturalPerson(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void requestNgoRegistration_debeCrearSolicitudPendiente_cuandoDatosValidos() {
        // Arrange
        NgoRegisterRequest request = new NgoRegisterRequest(
                "Patitas Felices", "ong@example.com", "Abcd1234",
                null, null, "data:application/pdf;base64,JVBERi0x", "Descripcion con al menos veinte caracteres",
                "Santiago", 2020, 10, 5
        );
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Abcd1234")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ngoRegistrationRequestRepository.save(any(NgoRegistrationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(List.of());

        // Act
        NgoRegistrationRequestResponse response = authService.requestNgoRegistration(request);

        // Assert
        assertEquals("Patitas Felices", response.ngoName());
        assertEquals("ong@example.com", response.email());
        assertEquals("data:application/pdf;base64,JVBERi0x", response.constitutionActUrl());

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals(UserRole.NGO, savedUser.getValue().getRole());
        assertEquals(AccountStatus.PENDING, savedUser.getValue().getStatus());
    }

    @Test
    void requestNgoRegistration_debeLanzarExcepcion_cuandoAnioFundacionEsFuturo() {
        // Arrange
        int anioFuturo = Year.now().getValue() + 1;
        NgoRegisterRequest request = new NgoRegisterRequest(
                "Patitas Felices", "ong@example.com", "Abcd1234",
                null, null, "data:application/pdf;base64,JVBERi0x", "Descripcion con al menos veinte caracteres",
                "Santiago", anioFuturo, 10, 5
        );
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        // Act + Assert
        assertThrows(InvalidRequestException.class, () -> authService.requestNgoRegistration(request));
        verify(userRepository, never()).save(any());
        verify(ngoRegistrationRequestRepository, never()).save(any());
    }
}
