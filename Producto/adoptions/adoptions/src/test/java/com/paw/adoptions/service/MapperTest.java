package com.paw.adoptions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.domain.AdoptionApplicationAnswer;
import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.domain.AnimalStatus;
import com.paw.adoptions.domain.FormFieldType;

class MapperTest {

    @Test
    void animalMapper_shouldReturnImmutablePhotoCopy() {
        // Arrange
        Animal animal = animal(UUID.randomUUID());
        animal.getPhotoUrls().add("https://img.test/a.jpg");
        AnimalMapper mapper = new AnimalMapper();

        // Act
        var response = mapper.toResponse(animal);

        // Assert
        assertEquals(List.of("https://img.test/a.jpg"), response.photoUrls());
        assertNotSame(animal.getPhotoUrls(), response.photoUrls());
        assertThrows(UnsupportedOperationException.class, () -> response.photoUrls().add("x"));
    }

    @Test
    void adoptionApplicationMapper_shouldUseFirstAnimalPhotoAndAnswerSnapshots() {
        // Arrange
        Animal animal = animal(UUID.randomUUID());
        animal.getPhotoUrls().add("https://img.test/main.jpg");
        animal.getPhotoUrls().add("https://img.test/second.jpg");
        AdoptionApplication application = new AdoptionApplication();
        application.setId(UUID.randomUUID());
        application.setAnimalId(animal.getId());
        application.setFullName("Persona Test");
        application.setEmail("persona@test.local");
        application.setPhone("912345678");
        application.setAddress("Santiago");
        application.setHousingType("Casa");
        application.setOtherAnimals("No");
        application.setMotivation("Quiero adoptar");
        application.setAvailability("Siempre");
        application.setPreviousExperience("Si");
        AdoptionApplicationAnswer answer = new AdoptionApplicationAnswer();
        answer.setFieldKey("custom_1");
        answer.setLabelSnapshot("Tiene patio?");
        answer.setTypeSnapshot(FormFieldType.BOOLEAN);
        answer.setAnswerValue("true");
        answer.setDisplayOrder(2);
        application.getCustomAnswers().add(answer);
        AdoptionApplicationMapper mapper = new AdoptionApplicationMapper();

        // Act
        var response = mapper.toResponse(application, animal);

        // Assert
        assertEquals("https://img.test/main.jpg", response.animalPhotoUrl());
        assertEquals(1, response.customAnswers().size());
        assertEquals("custom_1", response.customAnswers().getFirst().key());
        assertEquals(FormFieldType.BOOLEAN, response.customAnswers().getFirst().type());
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