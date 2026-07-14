package com.paw.adoptions.controller;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.adoptions.common.ApiResponse;
import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.domain.AdoptionApplicationStatus;
import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.domain.AnimalStatus;
import com.paw.adoptions.dto.AdoptionStatsResponse;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AdoptionApplicationRepository;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.AuthenticatedUser;
import com.paw.adoptions.security.CurrentUserProvider;
import com.paw.adoptions.security.UserRole;
import com.paw.adoptions.service.UserAccessValidator;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/adoptions/stats")
@RequiredArgsConstructor
public class AdoptionStatsController {

    private final AdoptionApplicationRepository applicationRepository;
    private final AnimalRepository animalRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserAccessValidator userAccessValidator;

    @GetMapping("/ngo")
    public ApiResponse<AdoptionStatsResponse> ngoStats() {
        AuthenticatedUser user = requireRole(UserRole.NGO);
        return ApiResponse.ok("Adoption stats found", buildStats(
                application -> application.getNgoId().equals(user.id()),
                animal -> animal.getNgoId().equals(user.id())
        ));
    }

    @GetMapping("/person")
    public ApiResponse<AdoptionStatsResponse> personStats() {
        AuthenticatedUser user = requireRole(UserRole.NATURAL_PERSON);
        return ApiResponse.ok("Adoption stats found", buildStats(
                application -> application.getPersonId().equals(user.id()),
                animal -> true
        ));
    }

    @GetMapping("/admin")
    public ApiResponse<AdoptionStatsResponse> adminStats() {
        requireRole(UserRole.ADMIN);
        return ApiResponse.ok("Adoption stats found", buildStats(application -> true, animal -> true));
    }

    private AuthenticatedUser requireRole(UserRole role) {
        AuthenticatedUser user = currentUserProvider.get();
        if (user.role() != role) {
            throw ApiException.forbidden("This operation requires role " + role);
        }
        userAccessValidator.requireActive(user);
        return user;
    }

    private AdoptionStatsResponse buildStats(
            Predicate<AdoptionApplication> applicationFilter,
            Predicate<Animal> animalFilter
    ) {
        List<AdoptionApplication> applications = applicationRepository.findAll().stream()
                .filter(applicationFilter)
                .toList();
        List<Animal> animals = animalRepository.findAll().stream()
                .filter(animalFilter)
                .toList();

        Map<String, Long> applicationsByStatus = toStatusMap(
                applications.stream().collect(Collectors.groupingBy(AdoptionApplication::getStatus, () -> new EnumMap<>(AdoptionApplicationStatus.class), Collectors.counting())),
                AdoptionApplicationStatus.values()
        );
        Map<String, Long> animalsByStatus = toStatusMap(
                animals.stream().collect(Collectors.groupingBy(Animal::getStatus, () -> new EnumMap<>(AnimalStatus.class), Collectors.counting())),
                AnimalStatus.values()
        );

        return new AdoptionStatsResponse(
                applications.size(),
                applicationsByStatus.getOrDefault(AdoptionApplicationStatus.PENDING.name(), 0L),
                applicationsByStatus.getOrDefault(AdoptionApplicationStatus.ACCEPTED.name(), 0L),
                applicationsByStatus.getOrDefault(AdoptionApplicationStatus.REJECTED.name(), 0L),
                applicationsByStatus,
                animalsByStatus.getOrDefault(AnimalStatus.AVAILABLE.name(), 0L),
                animalsByStatus.getOrDefault(AnimalStatus.ADOPTED.name(), 0L),
                animalsByStatus
        );
    }

    private <E extends Enum<E>> Map<String, Long> toStatusMap(Map<E, Long> source, E[] values) {
        return java.util.Arrays.stream(values)
                .collect(Collectors.toMap(Enum::name, value -> source.getOrDefault(value, 0L), (a, b) -> a, java.util.LinkedHashMap::new));
    }
}