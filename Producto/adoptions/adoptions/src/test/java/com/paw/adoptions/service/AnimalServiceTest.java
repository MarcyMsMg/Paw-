package com.paw.adoptions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import com.paw.adoptions.client.UserAccessResponse;
import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.domain.AnimalStatus;
import com.paw.adoptions.dto.AnimalCreateRequest;
import com.paw.adoptions.dto.AnimalUpdateRequest;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.AccountStatus;
import com.paw.adoptions.security.AuthenticatedUser;
import com.paw.adoptions.security.CurrentUserProvider;
import com.paw.adoptions.security.UserRole;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock private AnimalRepository animalRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private UserAccessValidator userAccessValidator;
    @Mock private AdoptionFormService adoptionFormService;

    private AnimalService animalService;
    private AnimalMapper animalMapper;
    private UUID ngoId;
    private AuthenticatedUser ngoUser;

    @BeforeEach
    void setUp() {
        animalMapper = new AnimalMapper();
        animalService = new AnimalService(
                animalRepository,
                animalMapper,
                currentUserProvider,
                userAccessValidator,
                adoptionFormService
        );
        ngoId = UUID.randomUUID();
        ngoUser = new AuthenticatedUser(ngoId, "ngo@test.local", UserRole.NGO);
    }

    @Test
    void create_shouldPersistAvailablePublishedAnimalWithCleanPhotosAndNgoLocation() {
        // Arrange
        UUID templateId = UUID.randomUUID();
        AnimalCreateRequest request = createRequest("   ", AnimalStatus.AVAILABLE, templateId);
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> {
            Animal animal = invocation.getArgument(0);
            animal.setId(UUID.randomUUID());
            return animal;
        });

        // Act
        var response = animalService.create(request);

        // Assert
        ArgumentCaptor<Animal> captor = ArgumentCaptor.forClass(Animal.class);
        verify(adoptionFormService).requireAssignableTemplate(templateId, ngoId);
        verify(animalRepository).save(captor.capture());
        Animal saved = captor.getValue();
        assertEquals(ngoId, saved.getNgoId());
        assertEquals("Santiago", saved.getLocation());
        assertEquals(AnimalStatus.AVAILABLE, saved.getStatus());
        assertTrue(saved.isPublished());
        assertEquals(List.of("https://img.test/a.jpg", "https://img.test/b.jpg"), saved.getPhotoUrls());
        assertEquals("Milo", response.name());
    }

    @Test
    void create_shouldRejectInitialStatusDifferentFromAvailable() {
        // Arrange
        AnimalCreateRequest request = createRequest("Valparaiso", AnimalStatus.IN_PROCESS, null);
        when(currentUserProvider.get()).thenReturn(ngoUser);

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> animalService.create(request));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }

    @Test
    void update_shouldTrimFieldsReplacePhotosAndTogglePublished() {
        // Arrange
        UUID animalId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        Animal animal = existingAnimal(animalId, ngoId);
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));
        AnimalUpdateRequest request = new AnimalUpdateRequest(
                "  Luna  ", "  Gato  ", "  2  ", "  Hembra  ", "  Pequeno  ", "  Nunoa  ",
                "  Sana  ", "  Descripcion suficientemente larga para validar  ", "  Casa segura  ",
                List.of("  https://img.test/c.jpg  ", "   "), AnimalStatus.IN_PROCESS, false, templateId
        );

        // Act
        var response = animalService.update(animalId, request);

        // Assert
        verify(adoptionFormService).requireAssignableTemplate(templateId, ngoId);
        assertEquals("Luna", animal.getName());
        assertEquals("Gato", animal.getSpecies());
        assertEquals("Nunoa", animal.getLocation());
        assertEquals(List.of("https://img.test/c.jpg"), animal.getPhotoUrls());
        assertEquals(AnimalStatus.IN_PROCESS, animal.getStatus());
        assertFalse(animal.isPublished());
        assertEquals("Luna", response.name());
    }

    @Test
    void update_shouldRejectAnimalOwnedByAnotherNgo() {
        // Arrange
        UUID animalId = UUID.randomUUID();
        Animal animal = existingAnimal(animalId, UUID.randomUUID());
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> animalService.update(animalId, updateRequest(AnimalStatus.AVAILABLE)));

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, exception.status());
    }

    @Test
    void retire_shouldHideAnimalAndMarkItRetired() {
        // Arrange
        UUID animalId = UUID.randomUUID();
        Animal animal = existingAnimal(animalId, ngoId);
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));

        // Act
        animalService.retire(animalId);

        // Assert
        assertFalse(animal.isPublished());
        assertEquals(AnimalStatus.RETIRED, animal.getStatus());
    }

    @Test
    void getPublic_shouldReturnOnlyPublishedNonRetiredAnimals() {
        // Arrange
        UUID animalId = UUID.randomUUID();
        Animal animal = existingAnimal(animalId, ngoId);
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));

        // Act
        var response = animalService.getPublic(animalId);

        // Assert
        assertEquals(animalId, response.id());
        assertEquals("Milo", response.name());
    }

    @Test
    void getPublic_shouldReturnNotFoundWhenAnimalIsUnpublished() {
        // Arrange
        UUID animalId = UUID.randomUUID();
        Animal animal = existingAnimal(animalId, ngoId);
        animal.setPublished(false);
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> animalService.getPublic(animalId));

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.status());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listPublic_shouldMapRepositoryResults() {
        // Arrange
        Animal animal = existingAnimal(UUID.randomUUID(), ngoId);
        when(animalRepository.findAll(any(Specification.class))).thenReturn(List.of(animal));

        // Act
        var response = animalService.listPublic(" Perro ", "Mediano", AnimalStatus.AVAILABLE, ngoId, "Santiago", "1");

        // Assert
        assertEquals(1, response.size());
        assertEquals("Milo", response.getFirst().name());
        verify(animalRepository).findAll(any(Specification.class));
    }

    @Test
    void listAllForAdmin_shouldRejectNonAdminUsers() {
        // Arrange
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> animalService.listAllForAdmin());

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, exception.status());
    }

    private AnimalCreateRequest createRequest(String location, AnimalStatus status, UUID templateId) {
        return new AnimalCreateRequest(
                "  Milo  ", "  Perro  ", "  1  ", "  Macho  ", "  Mediano  ", location,
                "  Sano  ", "  Descripcion suficientemente larga para validar  ", "  Patio seguro  ",
                List.of("  https://img.test/a.jpg  ", " ", "https://img.test/b.jpg"), status, templateId
        );
    }

    private AnimalUpdateRequest updateRequest(AnimalStatus status) {
        return new AnimalUpdateRequest(
                "Milo", "Perro", "1", "Macho", "Mediano", "Santiago", "Sano",
                "Descripcion suficientemente larga para validar", "Patio seguro",
                List.of("https://img.test/a.jpg"), status, true, null
        );
    }

    private Animal existingAnimal(UUID animalId, UUID ownerNgoId) {
        Animal animal = new Animal();
        animal.setId(animalId);
        animal.setNgoId(ownerNgoId);
        animal.setName("Milo");
        animal.setSpecies("Perro");
        animal.setAge("1");
        animal.setSex("Macho");
        animal.setSize("Mediano");
        animal.setLocation("Santiago");
        animal.setHealthStatus("Sano");
        animal.setDescription("Descripcion suficientemente larga para validar");
        animal.setAdoptionRequirements("Patio seguro");
        animal.setStatus(AnimalStatus.AVAILABLE);
        animal.setPublished(true);
        animal.getPhotoUrls().add("https://img.test/a.jpg");
        return animal;
    }
}