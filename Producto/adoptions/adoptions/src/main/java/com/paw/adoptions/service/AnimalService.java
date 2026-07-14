package com.paw.adoptions.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paw.adoptions.client.UserAccessResponse;
import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.domain.AnimalStatus;
import com.paw.adoptions.dto.AnimalCreateRequest;
import com.paw.adoptions.dto.AnimalResponse;
import com.paw.adoptions.dto.AnimalUpdateRequest;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.AuthenticatedUser;
import com.paw.adoptions.security.CurrentUserProvider;
import com.paw.adoptions.security.UserRole;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;
    private final CurrentUserProvider currentUserProvider;
    private final UserAccessValidator userAccessValidator;
    private final AdoptionFormService adoptionFormService;

    @Transactional(readOnly = true)
    public List<AnimalResponse> listPublic(
            String species,
            String size,
            AnimalStatus status,
            UUID ngoId,
            String location,
            String age
    ) {
        return animalRepository.findAll(publicSpecification(species, size, status, ngoId, location, age))
                .stream()
                .map(animalMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimalResponse getPublic(UUID animalId) {
        Animal animal = animalRepository.findById(animalId)
                .filter(Animal::isPublished)
                .filter(item -> item.getStatus() != AnimalStatus.RETIRED)
                .orElseThrow(() -> ApiException.notFound("Animal not found"));
        return animalMapper.toResponse(animal);
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> listOwned() {
        AuthenticatedUser user = requireActiveNgo();
        return animalRepository.findByNgoIdOrderByCreatedAtDesc(user.id())
                .stream()
                .map(animalMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> listAllForAdmin() {
        AuthenticatedUser user = currentUserProvider.get();
        userAccessValidator.requireActive(user);
        requireRole(user, UserRole.ADMIN);
        return animalRepository.findAll().stream().map(animalMapper::toResponse).toList();
    }

    @Transactional
    public AnimalResponse create(AnimalCreateRequest request) {
        AuthenticatedUser user = currentUserProvider.get();
        requireRole(user, UserRole.NGO);
        UserAccessResponse ngo = userAccessValidator.requireActive(user);

        if (request.status() != null && request.status() != AnimalStatus.AVAILABLE) {
            throw ApiException.badRequest("A new animal must start as AVAILABLE");
        }

        Animal animal = new Animal();
        animal.setNgoId(user.id());
        adoptionFormService.requireAssignableTemplate(request.formTemplateId(), user.id());
        applyRequest(animal, request);
        animal.setLocation(firstNonBlank(request.location(), ngo.location()));
        animal.setStatus(AnimalStatus.AVAILABLE);
        animal.setPublished(true);
        return animalMapper.toResponse(animalRepository.save(animal));
    }

    @Transactional
    public AnimalResponse update(UUID animalId, AnimalUpdateRequest request) {
        AuthenticatedUser user = requireActiveNgo();
        Animal animal = getOwnedAnimal(animalId, user.id());

        adoptionFormService.requireAssignableTemplate(request.formTemplateId(), user.id());

        if (request.status() == AnimalStatus.ADOPTED || request.status() == AnimalStatus.RETIRED) {
            throw ApiException.badRequest("ADOPTED and RETIRED statuses are managed by adoption workflows");
        }

        animal.setName(request.name().trim());
        animal.setSpecies(request.species().trim());
        animal.setAge(request.age().trim());
        animal.setSex(request.sex().trim());
        animal.setSize(request.size().trim());
        animal.setLocation(trimToNull(request.location()));
        animal.setHealthStatus(trimToNull(request.healthStatus()));
        animal.setDescription(request.description().trim());
        animal.setAdoptionRequirements(trimToNull(request.adoptionRequirements()));
        animal.setFormTemplateId(request.formTemplateId());
        replacePhotos(animal, request.photoUrls());
        if (request.status() != null) {
            animal.setStatus(request.status());
        }
        if (request.published() != null) {
            animal.setPublished(request.published());
        }
        return animalMapper.toResponse(animal);
    }

    @Transactional
    public void retire(UUID animalId) {
        AuthenticatedUser user = requireActiveNgo();
        Animal animal = getOwnedAnimal(animalId, user.id());
        animal.setPublished(false);
        animal.setStatus(AnimalStatus.RETIRED);
    }

    private AuthenticatedUser requireActiveNgo() {
        AuthenticatedUser user = currentUserProvider.get();
        requireRole(user, UserRole.NGO);
        userAccessValidator.requireActive(user);
        return user;
    }

    private Animal getOwnedAnimal(UUID animalId, UUID ngoId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> ApiException.notFound("Animal not found"));
        if (!animal.getNgoId().equals(ngoId)) {
            throw ApiException.forbidden("You cannot manage animals from another NGO");
        }
        return animal;
    }

    private void applyRequest(Animal animal, AnimalCreateRequest request) {
        animal.setName(request.name().trim());
        animal.setSpecies(request.species().trim());
        animal.setAge(request.age().trim());
        animal.setSex(request.sex().trim());
        animal.setSize(request.size().trim());
        animal.setHealthStatus(trimToNull(request.healthStatus()));
        animal.setDescription(request.description().trim());
        animal.setAdoptionRequirements(trimToNull(request.adoptionRequirements()));
        animal.setFormTemplateId(request.formTemplateId());
        animal.setPhotoUrls(cleanPhotos(request.photoUrls()));
    }

    private List<String> cleanPhotos(List<String> photos) {
        if (photos == null) {
            return new ArrayList<>();
        }
        return photos.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private void replacePhotos(Animal animal, List<String> photos) {
        animal.getPhotoUrls().clear();
        animal.getPhotoUrls().addAll(cleanPhotos(photos));
    }

    private Specification<Animal> publicSpecification(
            String species,
            String size,
            AnimalStatus status,
            UUID ngoId,
            String location,
            String age
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isTrue(root.get("published")));
            predicates.add(criteriaBuilder.notEqual(root.get("status"), AnimalStatus.RETIRED));
            addExactIgnoreCase(predicates, criteriaBuilder, root.get("species"), species);
            addExactIgnoreCase(predicates, criteriaBuilder, root.get("size"), size);
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("location"), location);
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("age"), age);
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (ngoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("ngoId"), ngoId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void addExactIgnoreCase(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            jakarta.persistence.criteria.Path<String> path,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            predicates.add(builder.equal(builder.lower(path), value.trim().toLowerCase()));
        }
    }

    private void addContainsIgnoreCase(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            jakarta.persistence.criteria.Path<String> path,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            predicates.add(builder.like(builder.lower(path), "%" + value.trim().toLowerCase() + "%"));
        }
    }

    private void requireRole(AuthenticatedUser user, UserRole requiredRole) {
        if (user.role() != requiredRole) {
            throw ApiException.forbidden("This operation requires role " + requiredRole);
        }
    }

    private String firstNonBlank(String first, String second) {
        String normalizedFirst = trimToNull(first);
        return normalizedFirst != null ? normalizedFirst : trimToNull(second);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
