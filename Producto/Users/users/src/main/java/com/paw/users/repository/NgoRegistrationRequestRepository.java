package com.paw.users.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.paw.users.enums.NgoRegistrationStatus;
import com.paw.users.model.NgoRegistrationRequest;

public interface NgoRegistrationRequestRepository extends JpaRepository<NgoRegistrationRequest, UUID> {

    @Query("""
            SELECT n
            FROM NgoRegistrationRequest n
            JOIN FETCH n.user
            """)
    List<NgoRegistrationRequest> findAllWithUser();

    @Query("""
            SELECT n
            FROM NgoRegistrationRequest n
            JOIN FETCH n.user
            WHERE n.status = :status
            """)
    List<NgoRegistrationRequest> findByStatusWithUser(NgoRegistrationStatus status);
}