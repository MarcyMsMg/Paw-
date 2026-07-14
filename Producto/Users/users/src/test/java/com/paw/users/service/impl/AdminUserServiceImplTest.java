package com.paw.users.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paw.users.client.NotificationsClient;
import com.paw.users.dto.request.UpdateUserStatusRequest;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.exception.InvalidRequestException;
import com.paw.users.model.User;
import com.paw.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationsClient notificationsClient;

    private AdminUserServiceImpl adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserServiceImpl(userRepository, notificationsClient);
    }

    @Test
    void updateStatus_debePermitirTransicion_deActiveADisabled() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User usuario = User.builder().id(userId).role(UserRole.NGO).status(AccountStatus.ACTIVE).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));

        // Act
        UserResponse response = adminUserService.updateStatus(userId, new UpdateUserStatusRequest(AccountStatus.DISABLED));

        // Assert
        assertEquals(AccountStatus.DISABLED, response.status());
    }

    @Test
    void updateStatus_debePermitirTransicion_deDisabledAActive() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User usuario = User.builder().id(userId).role(UserRole.NGO).status(AccountStatus.DISABLED).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));

        // Act
        UserResponse response = adminUserService.updateStatus(userId, new UpdateUserStatusRequest(AccountStatus.ACTIVE));

        // Assert
        assertEquals(AccountStatus.ACTIVE, response.status());
    }

    @Test
    void updateStatus_debeRechazarTransicion_dePendingAActive() {
        // Arrange: PENDING debe pasar por el flujo de aprobacion de solicitudes, no por este endpoint
        UUID userId = UUID.randomUUID();
        User usuario = User.builder().id(userId).role(UserRole.NGO).status(AccountStatus.PENDING).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));

        // Act + Assert
        assertThrows(
                InvalidRequestException.class,
                () -> adminUserService.updateStatus(userId, new UpdateUserStatusRequest(AccountStatus.ACTIVE))
        );
    }

    @Test
    void updateStatus_debeRechazarTransicion_deActiveAPending() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User usuario = User.builder().id(userId).role(UserRole.NGO).status(AccountStatus.ACTIVE).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));

        // Act + Assert
        assertThrows(
                InvalidRequestException.class,
                () -> adminUserService.updateStatus(userId, new UpdateUserStatusRequest(AccountStatus.PENDING))
        );
    }

    @Test
    void updateStatus_debeRechazarTransicion_deRejectedADisabled() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User usuario = User.builder().id(userId).role(UserRole.NGO).status(AccountStatus.REJECTED).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));

        // Act + Assert
        assertThrows(
                InvalidRequestException.class,
                () -> adminUserService.updateStatus(userId, new UpdateUserStatusRequest(AccountStatus.DISABLED))
        );
    }
}
