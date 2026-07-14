package com.paw.campaigns.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.campaigns.enums.CampaignStatus;
import com.paw.campaigns.model.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    List<Campaign> findByNgoId(UUID ngoId);

    // Campañas visibles públicamente: activas y dentro de su ventana de fechas
    // (ya comenzaron y aún no terminan).
    List<Campaign> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            CampaignStatus status, LocalDate start, LocalDate end);
}
