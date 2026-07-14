package com.paw.adoptions.strategy;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.repository.AdoptionApplicationRepository;
import com.paw.adoptions.security.UserRole;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminApplicationListingStrategy implements ApplicationListingStrategy {

    private final AdoptionApplicationRepository applicationRepository;

    @Override
    public UserRole supportedRole() {
        return UserRole.ADMIN;
    }

    @Override
    public List<AdoptionApplication> findApplications(UUID userId) {
        return applicationRepository.findAllByOrderByCreatedAtDesc();
    }
}
