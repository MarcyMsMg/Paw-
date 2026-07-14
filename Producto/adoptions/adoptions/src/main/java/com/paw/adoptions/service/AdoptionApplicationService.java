package com.paw.adoptions.service;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.domain.Animal;
import com.paw.adoptions.dto.AdoptionApplicationResponse;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.AuthenticatedUser;
import com.paw.adoptions.security.CurrentUserProvider;
import com.paw.adoptions.strategy.ApplicationListingStrategyResolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdoptionApplicationService {

    private final ApplicationListingStrategyResolver strategyResolver;
    private final AnimalRepository animalRepository;
    private final AdoptionApplicationMapper applicationMapper;
    private final CurrentUserProvider currentUserProvider;
    private final UserAccessValidator userAccessValidator;

    @Transactional(readOnly = true)
    public java.util.List<AdoptionApplicationResponse> listForCurrentUser() {
        AuthenticatedUser user = currentUserProvider.get();
        userAccessValidator.requireActive(user);

        java.util.List<AdoptionApplication> applications = strategyResolver.resolve(user.role())
                .findApplications(user.id());

        Map<UUID, Animal> animals = animalRepository.findAllById(
                        applications.stream().map(AdoptionApplication::getAnimalId).collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.toMap(Animal::getId, Function.identity()));

        return applications.stream()
                .map(application -> applicationMapper.toResponse(
                        application,
                        java.util.Optional.ofNullable(animals.get(application.getAnimalId()))
                                .orElseThrow(() -> ApiException.notFound("Animal not found for application"))
                ))
                .toList();
    }
}
