package com.paw.users;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.model.User;
import com.paw.users.security.AuthTokenManager;

class AuthTokenManagerTest {

    @Test
    void shouldGenerateAndValidateSignedJwt() {
        AuthTokenManager tokenManager = new AuthTokenManager(
                "test-secret-key-with-at-least-32-bytes",
                30
        );

        User user = User.builder()
                .id(java.util.UUID.randomUUID())
                .email("person@test.local")
                .role(UserRole.NATURAL_PERSON)
                .status(AccountStatus.ACTIVE)
                .build();

        var authenticatedUser = tokenManager.parseToken(tokenManager.generateToken(user));

        assertEquals(user.getId(), authenticatedUser.id());
        assertEquals(user.getEmail(), authenticatedUser.email());
        assertEquals(user.getRole(), authenticatedUser.role());
    }
}
