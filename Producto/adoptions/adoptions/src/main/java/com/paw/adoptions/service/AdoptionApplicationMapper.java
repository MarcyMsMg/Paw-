package com.paw.adoptions.service;

import org.springframework.stereotype.Component;

import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.dto.AdoptionApplicationResponse;
import com.paw.adoptions.dto.ApplicationAnswerResponse;

@Component
public class AdoptionApplicationMapper {

    public AdoptionApplicationResponse toResponse(AdoptionApplication application, Animal animal) {
        String photoUrl = animal.getPhotoUrls() == null || animal.getPhotoUrls().isEmpty()
                ? null
                : animal.getPhotoUrls().getFirst();

        return new AdoptionApplicationResponse(
                application.getId(),
                application.getAnimalId(),
                animal.getName(),
                photoUrl,
                application.getFullName(),
                application.getEmail(),
                application.getPhone(),
                application.getAddress(),
                application.getHousingType(),
                application.getOtherAnimals(),
                application.getMotivation(),
                application.getAvailability(),
                application.getPreviousExperience(),
                application.getFormTemplateId(),
                application.getFormTemplateRevision(),
                application.getCustomAnswers().stream()
                        .map(answer -> new ApplicationAnswerResponse(
                                answer.getFieldKey(),
                                answer.getLabelSnapshot(),
                                answer.getTypeSnapshot(),
                                answer.getAnswerValue(),
                                answer.getDisplayOrder()
                        ))
                        .toList(),
                application.getStatus(),
                application.getNgoResponse(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
