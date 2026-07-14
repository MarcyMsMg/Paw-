package com.paw.adoptions.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.paw.adoptions.client.NotificationsClient;
import com.paw.adoptions.client.UserAccessResponse;
import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.domain.AdoptionApplicationStatus;
import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.domain.AnimalStatus;
import com.paw.adoptions.dto.AdoptionApplicationCreateRequest;
import com.paw.adoptions.dto.AdoptionApplicationDecisionRequest;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AdoptionApplicationRepository;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.AccountStatus;
import com.paw.adoptions.security.AuthenticatedUser;
import com.paw.adoptions.security.CurrentUserProvider;
import com.paw.adoptions.security.UserRole;
import com.paw.adoptions.service.AdoptionApplicationMapper;
import com.paw.adoptions.service.AdoptionFormService;
import com.paw.adoptions.service.UserAccessValidator;

@ExtendWith(MockitoExtension.class)
class AdoptionFacadeTest {

    @Mock AnimalRepository animalRepository;
    @Mock AdoptionApplicationRepository applicationRepository;
    @Mock AdoptionApplicationMapper applicationMapper;
    @Mock CurrentUserProvider currentUserProvider;
    @Mock UserAccessValidator userAccessValidator;
    @Mock AdoptionFormService adoptionFormService;
    @Mock NotificationsClient notificationsClient;

    private AdoptionFacade adoptionFacade;

    @BeforeEach
    void setUp() {
        adoptionFacade = new AdoptionFacade(
                animalRepository,
                applicationRepository,
                applicationMapper,
                currentUserProvider,
                userAccessValidator,
                adoptionFormService,
                notificationsClient
        );
    }

    @Test
    void shouldRejectDuplicateActiveApplication() {
        UUID personId = UUID.randomUUID();
        UUID animalId = UUID.randomUUID();
        AuthenticatedUser person = new AuthenticatedUser(personId, "person@test.local", UserRole.NATURAL_PERSON);
        Animal animal = new Animal();
        animal.setId(animalId);
        animal.setNgoId(UUID.randomUUID());
        animal.setPublished(true);
        animal.setStatus(AnimalStatus.AVAILABLE);

        when(currentUserProvider.get()).thenReturn(person);
        when(userAccessValidator.requireActive(person)).thenReturn(new UserAccessResponse(
                personId, person.email(), person.role(), AccountStatus.ACTIVE, null, null
        ));
        when(animalRepository.findByIdForUpdate(animalId)).thenReturn(Optional.of(animal));
        when(applicationRepository.existsByAnimalIdAndPersonIdAndStatusIn(
                animalId,
                personId,
                List.of(AdoptionApplicationStatus.PENDING, AdoptionApplicationStatus.INFO_REQUESTED)
        )).thenReturn(true);

        AdoptionApplicationCreateRequest request = new AdoptionApplicationCreateRequest(
                "Test Person", "person@test.local", "912345678", "Test address",
                "Apartment", "No", "I want to adopt", "Weekends", "Previous dog experience"
        );

        ApiException exception = assertThrows(
                ApiException.class,
                () -> adoptionFacade.submitApplication(animalId, request)
        );
        assertEquals(HttpStatus.CONFLICT, exception.status());
    }

    @Test
    void shouldAdoptAnimalAndRejectOtherActiveApplications() {
        UUID ngoId = UUID.randomUUID();
        UUID animalId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        AuthenticatedUser ngo = new AuthenticatedUser(ngoId, "ngo@test.local", UserRole.NGO);

        Animal animal = new Animal();
        animal.setId(animalId);
        animal.setNgoId(ngoId);
        animal.setStatus(AnimalStatus.IN_PROCESS);

        AdoptionApplication accepted = new AdoptionApplication();
        accepted.setId(applicationId);
        accepted.setAnimalId(animalId);
        accepted.setNgoId(ngoId);
        accepted.setStatus(AdoptionApplicationStatus.PENDING);

        AdoptionApplication other = new AdoptionApplication();
        other.setId(UUID.randomUUID());
        other.setAnimalId(animalId);
        other.setNgoId(ngoId);
        other.setStatus(AdoptionApplicationStatus.PENDING);

        when(currentUserProvider.get()).thenReturn(ngo);
        when(userAccessValidator.requireActive(ngo)).thenReturn(new UserAccessResponse(
                ngoId, ngo.email(), ngo.role(), AccountStatus.ACTIVE, "Test NGO", "Santiago"
        ));
        when(applicationRepository.findByIdAndNgoId(applicationId, ngoId)).thenReturn(Optional.of(accepted));
        when(animalRepository.findByIdForUpdate(animalId)).thenReturn(Optional.of(animal));
        when(applicationRepository.saveAndFlush(accepted)).thenReturn(accepted);
        when(applicationRepository.findByAnimalIdAndStatusIn(
                animalId,
                List.of(AdoptionApplicationStatus.PENDING, AdoptionApplicationStatus.INFO_REQUESTED)
        )).thenReturn(List.of(other));

        adoptionFacade.decideApplication(
                applicationId,
                new AdoptionApplicationDecisionRequest(AdoptionApplicationStatus.ACCEPTED, "Approved")
        );

        assertEquals(AdoptionApplicationStatus.ACCEPTED, accepted.getStatus());
        assertEquals(AdoptionApplicationStatus.REJECTED, other.getStatus());
        assertEquals(AnimalStatus.ADOPTED, animal.getStatus());
    }
}