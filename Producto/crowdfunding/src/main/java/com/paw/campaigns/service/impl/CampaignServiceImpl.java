package com.paw.campaigns.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.paw.campaigns.client.NgoClient;
import com.paw.campaigns.client.NotificationsClient;
import com.paw.campaigns.client.dto.NgoData;
import com.paw.campaigns.client.dto.NotificationEventRequest;
import com.paw.campaigns.dto.request.CreateCampaignRequest;
import com.paw.campaigns.dto.request.UpdateCampaignRequest;
import com.paw.campaigns.dto.response.CampaignProgressItem;
import com.paw.campaigns.dto.response.CampaignResponse;
import com.paw.campaigns.dto.response.CampaignStatsResponse;
import com.paw.campaigns.enums.CampaignStatus;
import com.paw.campaigns.exception.NgoNotEligibleException;
import com.paw.campaigns.exception.ResourceNotFoundException;
import com.paw.campaigns.model.Campaign;
import com.paw.campaigns.repository.CampaignRepository;
import com.paw.campaigns.service.CampaignService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private static final String NGO_STATUS_ACTIVE = "ACTIVE";

    private final CampaignRepository campaignRepository;
    private final NgoClient ngoClient;
    private final NotificationsClient notificationsClient;

    @Override
    public List<CampaignResponse> findAllActive() {
        LocalDate today = LocalDate.now();
        return campaignRepository
                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        CampaignStatus.ACTIVE, today, today)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CampaignResponse> findByNgo(UUID ngoId) {
        return campaignRepository.findByNgoId(ngoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CampaignResponse findById(UUID id) {
        return toResponse(buscarOLanzar(id));
    }

    @Override
    @Transactional
    public CampaignResponse create(CreateCampaignRequest request) {
        NgoData ngo = ngoClient.getNgo(request.ngoId());
        if (!NGO_STATUS_ACTIVE.equals(ngo.status())) {
            throw new NgoNotEligibleException("La ONG no esta activa y no puede crear campanas");
        }

        validateDates(request.startDate(), request.endDate());

        Campaign campaign = Campaign.builder()
                .ngoId(request.ngoId())
                .title(request.title().trim())
                .description(request.description().trim())
                .bannerUrl(request.bannerUrl())
                .videoUrl(request.videoUrl())
                .category(request.category() == null ? null : request.category().trim())
                .goalAmount(request.goalAmount())
                .raisedAmount(BigDecimal.ZERO)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(CampaignStatus.ACTIVE)
                .build();

        Campaign saved = campaignRepository.save(campaign);
        notifyCampaign(saved, "CAMPAIGN_CREATED", "Campana creada", "Tu campana fue creada correctamente.", "/ong/campanas", "campaigns.created." + saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CampaignResponse update(UUID id, UpdateCampaignRequest request) {
        Campaign campaign = buscarOLanzar(id);

        if (request.title() != null) {
            campaign.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            campaign.setDescription(request.description().trim());
        }
        if (request.bannerUrl() != null) {
            campaign.setBannerUrl(request.bannerUrl());
        }
        if (request.videoUrl() != null) {
            campaign.setVideoUrl(request.videoUrl());
        }
        if (request.category() != null) {
            campaign.setCategory(request.category().trim());
        }
        if (request.startDate() != null) {
            campaign.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            campaign.setEndDate(request.endDate());
        }
        validateDates(campaign.getStartDate(), campaign.getEndDate());
        notifyCampaign(campaign, "CAMPAIGN_UPDATED", "Campana actualizada", "Tu campana fue actualizada.", "/ong/campanas", "campaigns.updated." + campaign.getId() + "." + Instant.now().toEpochMilli());
        return toResponse(campaign);
    }

    @Override
    @Transactional
    public CampaignResponse finish(UUID id) {
        Campaign campaign = buscarOLanzar(id);
        CampaignStatus previous = campaign.getStatus();
        campaign.setStatus(CampaignStatus.COMPLETED);
        if (previous != CampaignStatus.COMPLETED) {
            notifyCampaign(campaign, "CAMPAIGN_FINISHED", "Campana finalizada", "Tu campana fue finalizada.", "/ong/campanas", "campaigns.finished." + campaign.getId());
        }
        return toResponse(campaign);
    }

    @Override
    @Transactional
    public CampaignResponse addRaisedAmount(UUID id, BigDecimal amount) {
        Campaign campaign = buscarOLanzar(id);
        boolean wasBelowGoal = campaign.getRaisedAmount().compareTo(campaign.getGoalAmount()) < 0;
        campaign.setRaisedAmount(campaign.getRaisedAmount().add(amount));

        if (campaign.getRaisedAmount().compareTo(campaign.getGoalAmount()) >= 0) {
            campaign.setStatus(CampaignStatus.COMPLETED);
            if (wasBelowGoal) {
                notifyCampaign(campaign, "CAMPAIGN_GOAL_REACHED", "Meta alcanzada", "Tu campana alcanzo su meta de recaudacion.", "/ong/campanas", "campaigns.goal_reached." + campaign.getId());
            }
        }

        return toResponse(campaign);
    }

    @Override
    @Transactional
    public CampaignResponse subtractRaisedAmount(UUID id, BigDecimal amount) {
        Campaign campaign = buscarOLanzar(id);
        BigDecimal nuevo = campaign.getRaisedAmount().subtract(amount);
        if (nuevo.compareTo(BigDecimal.ZERO) < 0) {
            nuevo = BigDecimal.ZERO;
        }
        campaign.setRaisedAmount(nuevo);
        return toResponse(campaign);
    }

    @Override
    public CampaignStatsResponse statsForNgo(UUID ngoId) {
        return buildStats(campaignRepository.findByNgoId(ngoId));
    }

    @Override
    public CampaignStatsResponse statsForAdmin() {
        return buildStats(campaignRepository.findAll());
    }

    private CampaignStatsResponse buildStats(List<Campaign> campaigns) {
        long active = campaigns.stream().filter(campaign -> campaign.getStatus() == CampaignStatus.ACTIVE).count();
        long finished = campaigns.stream().filter(campaign -> campaign.getStatus() == CampaignStatus.COMPLETED).count();
        BigDecimal totalGoal = campaigns.stream().map(Campaign::getGoalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRaised = campaigns.stream().map(Campaign::getRaisedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<CampaignProgressItem> progress = campaigns.stream().map(this::toProgressItem).toList();
        BigDecimal averageProgress = progress.isEmpty()
                ? BigDecimal.ZERO
                : progress.stream().map(CampaignProgressItem::progress).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(progress.size()), 2, RoundingMode.HALF_UP);
        return new CampaignStatsResponse(active, finished, totalGoal, totalRaised, averageProgress, progress);
    }

    private CampaignProgressItem toProgressItem(Campaign campaign) {
        BigDecimal progress = BigDecimal.ZERO;
        if (campaign.getGoalAmount() != null && campaign.getGoalAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = campaign.getRaisedAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(campaign.getGoalAmount(), 2, RoundingMode.HALF_UP);
        }
        return new CampaignProgressItem(
                campaign.getId(),
                campaign.getTitle(),
                campaign.getGoalAmount(),
                campaign.getRaisedAmount(),
                progress,
                campaign.getStatus()
        );
    }

    private Campaign buscarOLanzar(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campana no encontrada"));
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    private void notifyCampaign(Campaign campaign, String type, String title, String message, String redirectUrl, String eventId) {
        notificationsClient.send(new NotificationEventRequest(
                eventId,
                type,
                Instant.now(),
                campaign.getNgoId(),
                "NGO",
                title,
                message,
                "CAMPAIGN",
                campaign.getId(),
                redirectUrl,
                campaignMetadata(campaign)
        ));
    }

    private String campaignMetadata(Campaign campaign) {
        return String.format(
                Locale.ROOT,
                "{\"title\":\"%s\",\"raisedAmount\":\"%s\",\"goalAmount\":\"%s\",\"status\":\"%s\"}",
                sanitize(campaign.getTitle()),
                campaign.getRaisedAmount(),
                campaign.getGoalAmount(),
                campaign.getStatus()
        );
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private CampaignResponse toResponse(Campaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getNgoId(),
                campaign.getTitle(),
                campaign.getDescription(),
                campaign.getBannerUrl(),
                campaign.getVideoUrl(),
                campaign.getCategory(),
                campaign.getGoalAmount(),
                campaign.getRaisedAmount(),
                campaign.getStartDate(),
                campaign.getEndDate(),
                campaign.getStatus(),
                campaign.getCreatedAt()
        );
    }
}