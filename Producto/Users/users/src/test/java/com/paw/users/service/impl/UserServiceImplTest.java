package com.paw.users.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paw.users.dto.request.UpdateUserProfileRequest;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.exception.InvalidRequestException;
import com.paw.users.exception.ResourceNotFoundException;
import com.paw.users.model.User;
import com.paw.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void updateProfile_debeActualizarNombreYApellido_cuandoEsPersonaNatural() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User usuario = User.builder()
                .id(userId)
                .role(UserRole.NATURAL_PERSON)
                .status(AccountStatus.ACTIVE)
                .firstName("Viejo")
                .lastName("Nombre")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Nuevo", "Apellido", null, null, null, null, null, null, null, null
        );

        // Act
        UserResponse response = userService.updateProfile(userId, request);

        // Assert
        assertEquals("Nuevo", response.firstName());
        assertEquals("Apellido", response.lastName());
    }

    @Test
    void updateProfile_debeActualizarCamposDeOng_cuandoEsRolNgo() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User usuario = User.builder()
                .id(userId)
                .role(UserRole.NGO)
                .status(AccountStatus.ACTIVE)
                .ngoName("ONG vieja")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                null, null, "ONG actualizada", null,
                "Descripcion con al menos veinte caracteres", null, "Valparaiso",
                2015, 20, 8
        );

        // Act
        UserResponse response = userService.updateProfile(userId, request);

        // Assert
        assertEquals("ONG actualizada", response.ngoName());
        assertEquals("Valparaiso", response.location());
        assertEquals(2015, response.foundationYear());
        assertEquals(20, response.rescuedAnimalsCount());
        assertEquals(8, response.volunteersCount());
    }

    @Test
    void updateProfile_debeLanzarExcepcion_cuandoAnioFundacionEsFuturo() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User usuario = User.builder()
                .id(userId)
                .role(UserRole.NGO)
                .status(AccountStatus.ACTIVE)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));

        int anioFuturo = Year.now().getValue() + 1;
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                null, null, null, null, null, null, null, anioFuturo, null, null
        );

        // Act + Assert
        assertThrows(InvalidRequestException.class, () -> userService.updateProfile(userId, request));
    }

    @Test
    void updateProfile_debeLanzarResourceNotFound_cuandoElUsuarioNoExiste() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Nombre", null, null, null, null, null, null, null, null, null
        );

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateProfile(userId, request));
    }

    @Test
    void updateProfile_noDebeModificarCampos_cuandoVienenComoNull() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User usuario = User.builder()
                .id(userId)
                .role(UserRole.NATURAL_PERSON)
                .status(AccountStatus.ACTIVE)
                .firstName("Original")
                .lastName("Apellido")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                null, null, null, null, null, null, null, null, null, null
        );

        // Act
        UserResponse response = userService.updateProfile(userId, request);

        // Assert
        assertEquals("Original", response.firstName());
        assertEquals("Apellido", response.lastName());
    }
}
