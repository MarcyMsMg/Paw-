package com.paw.donations.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paw.donations.enums.DonationStatus;
import com.paw.donations.model.Donation;

public interface DonationRepository extends JpaRepository<Donation, UUID> {

    List<Donation> findByDonorId(UUID donorId);

    List<Donation> findByCampaignId(UUID campaignId);

    List<Donation> findByNgoId(UUID ngoId);

    List<Donation> findByStatus(DonationStatus status);

    @Query("select count(distinct d.donorId) from Donation d where d.campaignId = :campaignId and d.status = :status")
    long countApprovedDonorsByCampaignId(@Param("campaignId") UUID campaignId, @Param("status") DonationStatus status);

    // Donaciones en un estado dado creadas antes de cierto momento (para expirar PENDING).
    List<Donation> findByStatusAndCreatedAtBefore(DonationStatus status, Instant cutoff);
}
