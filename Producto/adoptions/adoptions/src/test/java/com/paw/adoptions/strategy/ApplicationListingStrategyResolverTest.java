package com.paw.adoptions.strategy;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.security.UserRole;

class ApplicationListingStrategyResolverTest {

    @Test
    void shouldResolveStrategyByRole() {
        ApplicationListingStrategy strategy = mock(ApplicationListingStrategy.class);
        when(strategy.supportedRole()).thenReturn(UserRole.NGO);

        ApplicationListingStrategyResolver resolver = new ApplicationListingStrategyResolver(List.of(strategy));

        assertSame(strategy, resolver.resolve(UserRole.NGO));
        assertThrows(ApiException.class, () -> resolver.resolve(UserRole.ADMIN));
    }
}
