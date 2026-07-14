package com.paw.adoptions.facade;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paw.adoptions.client.NotificationsClient;
import com.paw.adoptions.client.UserAccessResponse;
import com.paw.adoptions.common.ChilePhoneValidator;
import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.domain.AdoptionApplicationStatus;
import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.domain.AnimalStatus;
import com.paw.adoptions.dto.AdoptionApplicationCreateRequest;
import com.paw.adoptions.dto.AdoptionApplicationDecisionRequest;
import com.paw.adoptions.dto.AdoptionApplicationResponse;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AdoptionApplicationRepository;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.AuthenticatedUser;
import com.paw.adoptions.security.CurrentUserProvider;
import com.paw.adoptions.security.UserRole;
import com.paw.adoptions.service.AdoptionApplicationMapper;
import com.paw.adoptions.service.AdoptionFormService;
import com.paw.adoptions.service.UserAccessValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdoptionFacade {

    private static final List<AdoptionApplicationStatus> ACTIVE_STATUSES = List.of(
            AdoptionApplicationStatus.PENDING,
            AdoptionApplicationStatus.INFO_REQUESTED
    );

    private final AnimalRepository animalRepository;
    private final AdoptionApplicationRepository applicationRepository;
    private final AdoptionApplicationMapper applicationMapper;
    private final CurrentUserProvider currentUserProvider;
    private final UserAccessValidator userAccessValidator;
    private final AdoptionFormService adoptionFormService;
    private final NotificationsClient notificationsClient;

    @Transactional
    public AdoptionApplicationResponse submitApplication(
            UUID animalId,
            AdoptionApplicationCreateRequest request
    ) {
        AuthenticatedUser authenticatedUser = currentUserProvider.get();
        requireRole(authenticatedUser, UserRole.NATURAL_PERSON);
        UserAccessResponse person = userAccessValidator.requireActive(authenticatedUser);

        if (!ChilePhoneValidator.isValid(request.phone())) {
            throw ApiException.badRequest("Phone must be a valid Chilean mobile number (e.g. 912345678)");
        }

        Animal animal = animalRepository.findByIdForUpdate(animalId)
                .orElseThrow(() -> ApiException.notFound("Animal not found"));

        if (!animal.isPublished()
                || animal.getStatus() == AnimalStatus.ADOPTED
                || animal.getStatus() == AnimalStatus.RETIRED) {
            throw ApiException.conflict("The animal is no longer available for adoption");
        }

        boolean duplicate = applicationRepository.existsByAnimalIdAndPersonIdAndStatusIn(
                animalId,
                authenticatedUser.id(),
                ACTIVE_STATUSES
        );
        if (duplicate) {
            throw ApiException.conflict("There is already an active application for this animal");
        }

        AdoptionApplication application = new AdoptionApplication();
        application.setAnimalId(animal.getId());
        application.setNgoId(animal.getNgoId());
        application.setPersonId(authenticatedUser.id());
        application.setFullName(request.fullName().trim());
        application.setEmail(person.email() == null ? request.email().trim() : person.email());
        application.setPhone(request.phone().trim());
        application.setAddress(request.address().trim());
        application.setHousingType(request.housingType().trim());
        application.setOtherAnimals(request.otherAnimals().trim());
        application.setMotivation(request.motivation().trim());
        application.setAvailability(trimToNull(request.availability()));
        application.setPreviousExperience(trimToNull(request.previousExperience()));
        application.setStatus(AdoptionApplicationStatus.PENDING);
        adoptionFormService.validateAndAttachAnswers(
                animal,
                request.customAnswers(),
                application
        );

        AdoptionApplication savedApplication = applicationRepository.save(application);

            if (animal.getStatus() == AnimalStatus.AVAILABLE) {
                animal.setStatus(AnimalStatus.IN_PROCESS);
                animalRepository.save(animal);
            }

            notifyApplicationCreated(savedApplication, animal);

            return applicationMapper.toResponse(savedApplication, animal);
            }

    @Transactional
    public AdoptionApplicationResponse decideApplication(
            UUID applicationId,
            AdoptionApplicationDecisionRequest request
    ) {
        AuthenticatedUser authenticatedUser = currentUserProvider.get();
        requireRole(authenticatedUser, UserRole.NGO);
        userAccessValidator.requireActive(authenticatedUser);

        AdoptionApplication application = applicationRepository
                .findByIdAndNgoId(applicationId, authenticatedUser.id())
                .orElseThrow(() -> ApiException.notFound("Adoption application not found"));

        if (!application.getStatus().isActive()) {
            throw ApiException.conflict("The adoption application has already been resolved");
        }

        Animal animal = animalRepository.findByIdForUpdate(application.getAnimalId())
                .orElseThrow(() -> ApiException.notFound("Animal not found"));

        validateDecision(request);
        application.setStatus(request.status());
        application.setNgoResponse(trimToNull(request.ngoResponse()));
        AdoptionApplication savedApplication = applicationRepository.saveAndFlush(application);

        switch (request.status()) {
            case ACCEPTED -> acceptApplication(savedApplication, animal);
            case REJECTED -> restoreAnimalWhenNoActiveApplications(animal);
            case INFO_REQUESTED -> animal.setStatus(AnimalStatus.IN_PROCESS);
            default -> throw ApiException.badRequest("Invalid adoption application decision");
        }

        animalRepository.save(animal);

        notifyApplicationDecision(savedApplication, animal);

        return applicationMapper.toResponse(savedApplication, animal);
    }


    private void notifyApplicationCreated(AdoptionApplication application, Animal animal) {
        notificationsClient.send(
                "adoptions.application.created." + application.getId(),
                "ADOPTION_APPLICATION_CREATED",
                application.getNgoId(),
                UserRole.NGO,
                "Nueva postulacion de adopcion",
                application.getFullName() + " postulo a " + animal.getName() + ".",
                "ADOPTION_APPLICATION",
                application.getId(),
                "/ong/solicitudes-adopcion",
                "{\"animalId\":\"" + animal.getId() + "\"}"
        );
    }

    private void notifyApplicationDecision(AdoptionApplication application, Animal animal) {
        String type = switch (application.getStatus()) {
            case ACCEPTED -> "ADOPTION_APPLICATION_ACCEPTED";
            case REJECTED -> "ADOPTION_APPLICATION_REJECTED";
            case INFO_REQUESTED -> "ADOPTION_APPLICATION_INFO_REQUESTED";
            default -> null;
        };
        if (type == null) {
            return;
        }

        String title = switch (application.getStatus()) {
            case ACCEPTED -> "Postulacion aceptada";
            case REJECTED -> "Postulacion rechazada";
            case INFO_REQUESTED -> "La ONG solicita mas informacion";
            default -> "Actualizacion de postulacion";
        };

        String message = switch (application.getStatus()) {
            case ACCEPTED -> "Tu postulacion por " + animal.getName() + " fue aceptada.";
            case REJECTED -> "Tu postulacion por " + animal.getName() + " fue rechazada.";
            case INFO_REQUESTED -> "La ONG necesita mas informacion sobre tu postulacion por " + animal.getName() + ".";
            default -> "Tu postulacion fue actualizada.";
        };

        notificationsClient.send(
                "adoptions.application.decision." + application.getId() + "." + application.getStatus(),
                type,
                application.getPersonId(),
                UserRole.NATURAL_PERSON,
                title,
                message,
                "ADOPTION_APPLICATION",
                application.getId(),
                "/persona/mis-postulaciones",
                "{\"animalId\":\"" + animal.getId() + "\"}"
        );
    }
    private void acceptApplication(AdoptionApplication acceptedApplication, Animal animal) {
        animal.setStatus(AnimalStatus.ADOPTED);

        List<AdoptionApplication> otherApplications = applicationRepository.findByAnimalIdAndStatusIn(
                animal.getId(),
                ACTIVE_STATUSES
        );

        for (AdoptionApplication application : otherApplications) {
            if (!application.getId().equals(acceptedApplication.getId())) {
                application.setStatus(AdoptionApplicationStatus.REJECTED);
                application.setNgoResponse("Another application was accepted for this animal.");
            }
        }
        applicationRepository.saveAll(otherApplications);
    }

    private void restoreAnimalWhenNoActiveApplications(Animal animal) {
        boolean hasActiveApplications = !applicationRepository
                .findByAnimalIdAndStatusIn(animal.getId(), ACTIVE_STATUSES)
                .isEmpty();

        if (!hasActiveApplications && animal.getStatus() == AnimalStatus.IN_PROCESS) {
            animal.setStatus(AnimalStatus.AVAILABLE);
        }
    }

    private void validateDecision(AdoptionApplicationDecisionRequest request) {
        if (request.status() != AdoptionApplicationStatus.ACCEPTED
                && request.status() != AdoptionApplicationStatus.REJECTED
                && request.status() != AdoptionApplicationStatus.INFO_REQUESTED) {
            throw ApiException.badRequest("Decision must be ACCEPTED, REJECTED or INFO_REQUESTED");
        }
        if (request.status() == AdoptionApplicationStatus.INFO_REQUESTED
                && (request.ngoResponse() == null || request.ngoResponse().isBlank())) {
            throw ApiException.badRequest("An NGO response is required when requesting more information");
        }
    }

    private void requireRole(AuthenticatedUser user, UserRole role) {
        if (user.role() != role) {
            throw ApiException.forbidden("This operation requires role " + role);
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
