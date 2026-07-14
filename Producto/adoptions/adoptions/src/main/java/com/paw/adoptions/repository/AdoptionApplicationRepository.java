package com.paw.adoptions.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.adoptions.domain.AdoptionApplication;
import com.paw.adoptions.domain.AdoptionApplicationStatus;

public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, UUID> {

    List<AdoptionApplication> findByPersonIdOrderByCreatedAtDesc(UUID personId);

    List<AdoptionApplication> findByNgoIdOrderByCreatedAtDesc(UUID ngoId);

    List<AdoptionApplication> findAllByOrderByCreatedAtDesc();

    Optional<AdoptionApplication> findByIdAndNgoId(UUID id, UUID ngoId);

    boolean existsByAnimalIdAndPersonIdAndStatusIn(
            UUID animalId,
            UUID personId,
            Collection<AdoptionApplicationStatus> statuses
    );

    List<AdoptionApplication> findByAnimalIdAndStatusIn(
            UUID animalId,
            Collection<AdoptionApplicationStatus> statuses
    );
}
