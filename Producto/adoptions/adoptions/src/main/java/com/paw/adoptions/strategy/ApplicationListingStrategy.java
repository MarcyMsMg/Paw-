package com.paw.adoptions.strategy;

import java.util.List;
import java.util.UUID;

import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.security.UserRole;

public interface ApplicationListingStrategy {

    UserRole supportedRole();

    List<AdoptionApplication> findApplications(UUID userId);
}
