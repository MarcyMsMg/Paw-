package com.paw.feed.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.paw.feed.client.UserAccessResponse;
import com.paw.feed.client.UsersClient;
import com.paw.feed.exception.ApiException;
import com.paw.feed.security.AccountStatus;
import com.paw.feed.security.AuthenticatedUser;
import com.paw.feed.security.UserRole;

@ExtendWith(MockitoExtension.class)
class UserAccessValidatorTest {

    @Mock private UsersClient usersClient;

    @Test
    void requireActive_shouldReturnUserWhenAccountIsActiveAndRoleMatches() {
        // Arrange
        UserAccessValidator validator = new UserAccessValidator(usersClient);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(UUID.randomUUID(), "ngo@test.local", UserRole.NGO);
        UserAccessResponse access = new UserAccessResponse(authenticatedUser.id(), authenticatedUser.email(), UserRole.NGO, AccountStatus.ACTIVE, "Patitas", "Santiago");
        when(usersClient.findAccess(authenticatedUser.id())).thenReturn(access);

        // Act
        UserAccessResponse result = validator.requireActive(authenticatedUser);

        // Assert
        assertSame(access, result);
    }

    @Test
    void requireActive_shouldRejectInactiveAccount() {
        // Arrange
        UserAccessValidator validator = new UserAccessValidator(usersClient);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(UUID.randomUUID(), "ngo@test.local", UserRole.NGO);
        when(usersClient.findAccess(authenticatedUser.id())).thenReturn(new UserAccessResponse(
                authenticatedUser.id(), authenticatedUser.email(), UserRole.NGO, AccountStatus.PENDING, "Patitas", "Santiago"
        ));

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> validator.requireActive(authenticatedUser));

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, exception.status());
    }

    @Test
    void requireActive_shouldRejectRoleMismatch() {
        // Arrange
        UserAccessValidator validator = new UserAccessValidator(usersClient);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(UUID.randomUUID(), "ngo@test.local", UserRole.NGO);
        when(usersClient.findAccess(authenticatedUser.id())).thenReturn(new UserAccessResponse(
                authenticatedUser.id(), authenticatedUser.email(), UserRole.ADMIN, AccountStatus.ACTIVE, "Admin", "Santiago"
        ));

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> validator.requireActive(authenticatedUser));

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exception.status());
    }
}