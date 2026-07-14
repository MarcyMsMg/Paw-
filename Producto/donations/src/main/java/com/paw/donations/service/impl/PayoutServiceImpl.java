package com.paw.donations.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.paw.donations.client.CampaignClient;
import com.paw.donations.client.UsersClient;
import com.paw.donations.client.dto.CampaignData;
import com.paw.donations.client.dto.UserData;
import com.paw.donations.dto.request.CreatePayoutRequest;
import com.paw.donations.dto.response.CampaignBalanceResponse;
import com.paw.donations.dto.response.NgoBalanceResponse;
import com.paw.donations.dto.response.PayoutResponse;
import com.paw.donations.enums.DonationStatus;
import com.paw.donations.exception.ResourceNotFoundException;
import com.paw.donations.model.Donation;
import com.paw.donations.model.Payout;
import com.paw.donations.repository.DonationRepository;
import com.paw.donations.repository.PayoutRepository;
import com.paw.donations.service.PayoutService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayoutServiceImpl implements PayoutService {

    private static final String CAMPAIGN_STATUS_COMPLETED = "COMPLETED";

    private final DonationRepository donationRepository;
    private final PayoutRepository payoutRepository;
    private final CampaignClient campaignClient;
    private final UsersClient usersClient;

    @Override
    public List<NgoBalanceResponse> getBalances() {
        // Solo se libera dinero de campañas FINALIZADAS (COMPLETED).
        List<Donation> approved = donationRepository.findByStatus(DonationStatus.APPROVED);
        Map<UUID, CampaignData> completed = completedCampaigns(approved);

        // Por ONG: total liberable y desglose por campaña.
        Map<UUID, BigDecimal> donatedByNgo = new HashMap<>();
        Map<UUID, Map<UUID, BigDecimal>> byNgoAndCampaign = new HashMap<>();
        for (Donation d : approved) {
            if (!isReleasable(d, completed.keySet())) {
                continue;
            }
            donatedByNgo.merge(d.getNgoId(), effectiveAmount(d), BigDecimal::add);
            byNgoAndCampaign
                    .computeIfAbsent(d.getNgoId(), k -> new LinkedHashMap<>())
                    .merge(d.getCampaignId(), effectiveAmount(d), BigDecimal::add);
        }

        // Total ya transferido por ONG.
        Map<UUID, BigDecimal> paidByNgo = new HashMap<>();
        for (Payout p : payoutRepository.findAll()) {
            paidByNgo.merge(p.getNgoId(), p.getAmount(), BigDecimal::add);
        }

        Set<UUID> ngoIds = new LinkedHashSet<>();
        ngoIds.addAll(donatedByNgo.keySet());
        ngoIds.addAll(paidByNgo.keySet());

        List<NgoBalanceResponse> balances = new ArrayList<>();
        for (UUID ngoId : ngoIds) {
            balances.add(buildBalance(
                    ngoId,
                    donatedByNgo.getOrDefault(ngoId, BigDecimal.ZERO),
                    paidByNgo.getOrDefault(ngoId, BigDecimal.ZERO),
                    campaignBreakdown(byNgoAndCampaign.getOrDefault(ngoId, Map.of()), completed)
            ));
        }
        return balances;
    }

    @Override
    public NgoBalanceResponse getBalance(UUID ngoId) {
        List<Donation> approved = donationRepository.findByNgoId(ngoId).stream()
                .filter(d -> d.getStatus() == DonationStatus.APPROVED)
                .toList();
        Map<UUID, CampaignData> completed = completedCampaigns(approved);

        Map<UUID, BigDecimal> byCampaign = new LinkedHashMap<>();
        BigDecimal donated = BigDecimal.ZERO;
        for (Donation d : approved) {
            if (!isReleasable(d, completed.keySet())) {
                continue;
            }
            donated = donated.add(effectiveAmount(d));
            byCampaign.merge(d.getCampaignId(), effectiveAmount(d), BigDecimal::add);
        }

        BigDecimal paid = payoutRepository.findByNgoId(ngoId).stream()
                .map(Payout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return buildBalance(ngoId, donated, paid, campaignBreakdown(byCampaign, completed));
    }

    @Override
    @Transactional
    public PayoutResponse createPayout(CreatePayoutRequest request) {
        NgoBalanceResponse balance = getBalance(request.ngoId());

        // No se puede transferir más de lo que se le debe a la ONG.
        if (request.amount().compareTo(balance.balanceOwed()) > 0) {
            throw new IllegalArgumentException(
                    "El monto excede el saldo pendiente de la ONG (disponible: " + balance.balanceOwed() + ")");
        }

        Payout payout = Payout.builder()
                .ngoId(request.ngoId())
                .amount(request.amount())
                .reference(request.reference())
                .note(request.note())
                .build();

        return toResponse(payoutRepository.save(payout));
    }

    @Override
    public List<PayoutResponse> findPayoutsByNgo(UUID ngoId) {
        return payoutRepository.findByNgoId(ngoId).stream().map(this::toResponse).toList();
    }

    // Consulta a Campañas el estado de cada campaña involucrada y devuelve un mapa
    // id -> datos de las que están COMPLETED (las únicas que liberan fondos a la ONG).
    private Map<UUID, CampaignData> completedCampaigns(List<Donation> donations) {
        Set<UUID> campaignIds = donations.stream()
                .map(Donation::getCampaignId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, CampaignData> completed = new HashMap<>();
        for (UUID campaignId : campaignIds) {
            try {
                CampaignData campaign = campaignClient.getCampaign(campaignId);
                if (campaign != null && CAMPAIGN_STATUS_COMPLETED.equalsIgnoreCase(campaign.status())) {
                    completed.put(campaignId, campaign);
                }
            } catch (ResourceNotFoundException ex) {
                // Campaña inexistente → no libera fondos.
            }
        }
        return completed;
    }

    // Arma el desglose por campaña con su título (resuelto desde Campañas).
    private List<CampaignBalanceResponse> campaignBreakdown(
            Map<UUID, BigDecimal> amountsByCampaign, Map<UUID, CampaignData> completed) {
        List<CampaignBalanceResponse> result = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> entry : amountsByCampaign.entrySet()) {
            CampaignData data = completed.get(entry.getKey());
            String title = data != null ? data.title() : null;
            result.add(new CampaignBalanceResponse(entry.getKey(), title, entry.getValue()));
        }
        return result;
    }

    private boolean isReleasable(Donation d, Set<UUID> releasableCampaigns) {
        return d.getCampaignId() != null && releasableCampaigns.contains(d.getCampaignId());
    }

    // Lo que se le puede transferir a la ONG es el NETO que MercadoPago acreditó
    // (bruto − comisión). Si faltara (donaciones antiguas), cae al bruto y luego al solicitado.
    private BigDecimal effectiveAmount(Donation d) {
        if (d.getNetAmount() != null) {
            return d.getNetAmount();
        }
        return d.getPaidAmount() != null ? d.getPaidAmount() : d.getAmount();
    }

    private NgoBalanceResponse buildBalance(UUID ngoId, BigDecimal donated, BigDecimal paid,
            List<CampaignBalanceResponse> campaigns) {
        return new NgoBalanceResponse(
                ngoId, resolveNgoName(ngoId), donated, paid, donated.subtract(paid), campaigns);
    }

    // Best-effort: si Usuarios no responde, el saldo se muestra igual sin el nombre.
    private String resolveNgoName(UUID ngoId) {
        UserData data = usersClient.getUser(ngoId);
        return data != null ? data.ngoName() : null;
    }

    private PayoutResponse toResponse(Payout p) {
        return new PayoutResponse(
                p.getId(),
                p.getNgoId(),
                p.getAmount(),
                p.getReference(),
                p.getNote(),
                p.getCreatedAt()
        );
    }
}
