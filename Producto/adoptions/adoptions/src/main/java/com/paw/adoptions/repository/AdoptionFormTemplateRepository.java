package com.paw.adoptions.repository;

import com.paw.adoptions.domain.AdoptionFormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdoptionFormTemplateRepository extends JpaRepository<AdoptionFormTemplate, UUID> {
    List<AdoptionFormTemplate> findByNgoIdOrderByUpdatedAtDesc(UUID ngoId);
    Optional<AdoptionFormTemplate> findByIdAndNgoId(UUID id, UUID ngoId);
    Optional<AdoptionFormTemplate> findByIdAndNgoIdAndActiveTrue(UUID id, UUID ngoId);
}
