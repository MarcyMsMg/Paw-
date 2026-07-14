package com.paw.adoptions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.paw.adoptions.client.UserAccessResponse;
import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.domain.AnimalStatus;
import com.paw.adoptions.dto.AdoptionApplicationResponse;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.AccountStatus;
import com.paw.adoptions.security.AuthenticatedUser;
import com.paw.adoptions.security.CurrentUserProvider;
import com.paw.adoptions.security.UserRole;
import com.paw.adoptions.strategy.ApplicationListingStrategy;
import com.paw.adoptions.strategy.ApplicationListingStrategyResolver;

@ExtendWith(MockitoExtension.class)
class AdoptionApplicationServiceTest {

    @Mock private ApplicationListingStrategyResolver strategyResolver;
    @Mock private ApplicationListingStrategy listingStrategy;
    @Mock private AnimalRepository animalRepository;
    @Mock private AdoptionApplicationMapper applicationMapper;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private UserAccessValidator userAccessValidator;

    private AdoptionApplicationService applicationService;
    private AuthenticatedUser person;

    @BeforeEach
    void setUp() {
        applicationService = new AdoptionApplicationService(
                strategyResolver,
                animalRepository,
                applicationMapper,
                currentUserProvider,
                userAccessValidator
        );
        person = new AuthenticatedUser(UUID.randomUUID(), "person@test.local", UserRole.NATURAL_PERSON);
    }

    @Test
    void listForCurrentUser_shouldResolveStrategyLoadAnimalsAndMapResponses() {
        // Arrange
        UUID animalId = UUID.randomUUID();
        AdoptionApplication application = new AdoptionApplication();
        application.setId(UUID.randomUUID());
        application.setAnimalId(animalId);
        Animal animal = animal(animalId);
        AdoptionApplicationResponse mapped = new AdoptionApplicationMapper().toResponse(application, animal);
        when(currentUserProvider.get()).thenReturn(person);
        when(userAccessValidator.requireActive(person)).thenReturn(new UserAccessResponse(
                person.id(), person.email(), person.role(), AccountStatus.ACTIVE, null, null
        ));
        when(strategyResolver.resolve(UserRole.NATURAL_PERSON)).thenReturn(listingStrategy);
        when(listingStrategy.findApplications(person.id())).thenReturn(List.of(application));
        when(animalRepository.findAllById(any())).thenReturn(List.of(animal));
        when(applicationMapper.toResponse(application, animal)).thenReturn(mapped);

        // Act
        var responses = applicationService.listForCurrentUser();

        // Assert
        assertEquals(1, responses.size());
        assertEquals(animalId, responses.getFirst().animalId());
        verify(strategyResolver).resolve(UserRole.NATURAL_PERSON);
    }

    @Test
    void listForCurrentUser_shouldFailWhenAnimalForApplicationIsMissing() {
        // Arrange
        UUID animalId = UUID.randomUUID();
        AdoptionApplication application = new AdoptionApplication();
        application.setId(UUID.randomUUID());
        application.setAnimalId(animalId);
        when(currentUserProvider.get()).thenReturn(person);
        when(userAccessValidator.requireActive(person)).thenReturn(new UserAccessResponse(
                person.id(), person.email(), person.role(), AccountStatus.ACTIVE, null, null
        ));
        when(strategyResolver.resolve(UserRole.NATURAL_PERSON)).thenReturn(listingStrategy);
        when(listingStrategy.findApplications(person.id())).thenReturn(List.of(application));
        when(animalRepository.findAllById(any())).thenReturn(List.of());

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> applicationService.listForCurrentUser());

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.status());
    }

    private Animal animal(UUID animalId) {
        Animal animal = new Animal();
        animal.setId(animalId);
        animal.setNgoId(UUID.randomUUID());
        animal.setName("Milo");
        animal.setSpecies("Perro");
        animal.setAge("1");
        animal.setSex("Macho");
        animal.setSize("Mediano");
        animal.setDescription("Descripcion suficientemente larga");
        animal.setStatus(AnimalStatus.AVAILABLE);
        animal.setPublished(true);
        return animal;
    }
}