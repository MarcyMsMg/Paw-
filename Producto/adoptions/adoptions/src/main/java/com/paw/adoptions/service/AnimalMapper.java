package com.paw.adoptions.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.dto.AnimalResponse;

@Component
public class AnimalMapper {

    public AnimalResponse toResponse(Animal animal) {
        return new AnimalResponse(
                animal.getId(),
                animal.getNgoId(),
                animal.getName(),
                animal.getSpecies(),
                animal.getAge(),
                animal.getSex(),
                animal.getSize(),
                animal.getLocation(),
                animal.getHealthStatus(),
                animal.getDescription(),
                animal.getAdoptionRequirements(),
                animal.getFormTemplateId(),
                animal.getPhotoUrls() == null ? List.of() : List.copyOf(animal.getPhotoUrls()),
                animal.getStatus(),
                animal.isPublished(),
                animal.getCreatedAt(),
                animal.getUpdatedAt()
        );
    }

}
