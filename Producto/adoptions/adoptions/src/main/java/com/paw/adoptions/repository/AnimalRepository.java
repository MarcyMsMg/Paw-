package com.paw.adoptions.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paw.adoptions.domain.Animal;

import jakarta.persistence.LockModeType;

public interface AnimalRepository extends JpaRepository<Animal, UUID>, JpaSpecificationExecutor<Animal> {

    List<Animal> findByNgoIdOrderByCreatedAtDesc(UUID ngoId);

    long countByFormTemplateId(UUID formTemplateId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select animal from Animal animal where animal.id = :id")
    Optional<Animal> findByIdForUpdate(@Param("id") UUID id);
}
