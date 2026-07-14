package com.paw.donations.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.paw.donations.client.CampaignClient;
import com.paw.donations.client.NotificationsClient;
import com.paw.donations.client.UsersClient;
import com.paw.donations.client.dto.CampaignData;
import com.paw.donations.client.dto.NotificationEventRequest;
import com.paw.donations.client.dto.UserData;
import com.paw.donations.dto.request.CreateDonationRequest;
import com.paw.donations.dto.response.CheckoutResponse;
import com.paw.donations.dto.response.CampaignDonationSummaryResponse;
import com.paw.donations.dto.response.DonationResponse;
import com.paw.donations.dto.response.DonationStatsResponse;
import com.paw.donations.dto.response.ReceiptResponse;
import com.paw.donations.enums.DonationStatus;
import com.paw.donations.enums.DonationType;
import com.paw.donations.exception.InvalidDonationException;
import com.paw.donations.exception.ResourceNotFoundException;
import com.paw.donations.model.Donation;
import com.paw.donations.repository.DonationRepository;
import com.paw.donations.service.DonationService;
import com.paw.donations.service.MercadoPagoService;
import com.paw.donations.service.MercadoPagoService.PaymentResult;
import com.paw.donations.service.MercadoPagoService.PreferenceResult;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final DonationRepository donationRepository;
    private final CampaignClient campaignClient;
    private final UsersClient usersClient;
    private final MercadoPagoService mercadoPagoService;
    private final NotificationsClient notificationsClient;

    @Override
    @Transactional
    public CheckoutResponse create(CreateDonationRequest request) {
        Donation donation = buildPendingDonation(request);

        Donation saved = donationRepository.save(donation);

        PreferenceResult preference = mercadoPagoService.createPreference(
                saved.getId(),
                buildTitle(saved),
                saved.getAmount()
        );

        saved.setPreferenceId(preference.preferenceId());

        notifyDonationCreated(saved);

        return new CheckoutResponse(saved.getId(), saved.getStatus(), preference.checkoutUrl());
    }

    @Override
    public DonationResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public ReceiptResponse getReceipt(UUID id) {
        Donation donation = getOrThrow(id);
        if (donation.getStatus() != DonationStatus.APPROVED) {
            throw new InvalidDonationException("La donación no está aprobada, no tiene comprobante");
        }

        UserData donor = usersClient.getUser(donation.getDonorId());
        UserData ngo = usersClient.getUser(donation.getNgoId());
        String campaignTitle = fetchCampaignTitle(donation.getCampaignId());

        return new ReceiptResponse(
                donation.getReceiptNumber(),
                donation.getId(),
                donation.getDonorId(),
                fullName(donor),
                donor != null ? donor.email() : null,
                donation.getCampaignId(),
                campaignTitle,
                donation.getNgoId(),
                ngo != null ? ngo.ngoName() : null,
                donation.getPaidAmount() != null ? donation.getPaidAmount() : donation.getAmount(),
                donation.getPaymentMethod(),
                donation.getPaidAt()
        );
    }

    @Override
    public List<DonationResponse> findByDonor(UUID donorId) {
        return donationRepository.findByDonorId(donorId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<DonationResponse> findByCampaign(UUID campaignId) {
        return donationRepository.findByCampaignId(campaignId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CampaignDonationSummaryResponse campaignSummary(UUID campaignId) {
        long donorCount = donationRepository.countApprovedDonorsByCampaignId(campaignId, DonationStatus.APPROVED);
        return new CampaignDonationSummaryResponse(donorCount);
    }

    @Override
    public List<DonationResponse> findByNgo(UUID ngoId) {
        return donationRepository.findByNgoId(ngoId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void handlePaymentNotification(String paymentId) {
        PaymentResult payment = mercadoPagoService.getPayment(paymentId);

        UUID donationId = parseDonationId(payment.externalReference());
        Donation donation = getOrThrow(donationId);

        donation.setExternalPaymentId(paymentId);
        if (payment.paymentType() != null) {
            donation.setPaymentMethod(payment.paymentType());
        }

        String mpStatus = payment.status() == null ? "" : payment.status().toLowerCase();
        DonationStatus previousStatus = donation.getStatus();

        switch (mpStatus) {
            case "approved" -> {
                if (previousStatus == DonationStatus.PENDING) {
                    BigDecimal paid = payment.amount() != null ? payment.amount() : donation.getAmount();
                    BigDecimal net = payment.netAmount() != null ? payment.netAmount() : paid;

                    donation.setStatus(DonationStatus.APPROVED);
                    donation.setPaidAmount(paid);
                    donation.setNetAmount(net);
                    donation.setPaidAt(Instant.now());
                    donation.setReceiptNumber(generateReceiptNumber(donation));

                    campaignClient.addRaisedAmount(donation.getCampaignId(), paid);
                }
            }
            case "refunded" -> {
                if (previousStatus == DonationStatus.APPROVED) {
                    campaignClient.lowerRaisedAmount(donation.getCampaignId(), creditedAmount(donation));
                }
                if (previousStatus != DonationStatus.REFUNDED) {
                    donation.setStatus(DonationStatus.REFUNDED);
                }
            }
            case "charged_back" -> {
                if (previousStatus == DonationStatus.APPROVED) {
                    campaignClient.lowerRaisedAmount(donation.getCampaignId(), creditedAmount(donation));
                }
                if (previousStatus != DonationStatus.CHARGED_BACK) {
                    donation.setStatus(DonationStatus.CHARGED_BACK);
                }
            }
            case "rejected", "cancelled" -> {
                if (previousStatus == DonationStatus.PENDING) {
                    donation.setStatus(DonationStatus.REJECTED);
                }
            }
            default -> {
                // pending / in_process: se mantiene como está.
            }
        }

        if (previousStatus != donation.getStatus()) {
            notifyDonationStatusChanged(donation, previousStatus, donation.getStatus());
        }
    }

    @Override
    public DonationStatsResponse statsForPerson(UUID donorId) {
        return buildStats(donationRepository.findByDonorId(donorId), true);
    }

    @Override
    public DonationStatsResponse statsForNgo(UUID ngoId) {
        return buildStats(donationRepository.findByNgoId(ngoId), false);
    }

    @Override
    public DonationStatsResponse statsForAdmin() {
        return buildStats(donationRepository.findAll(), false);
    }

    private Donation buildPendingDonation(CreateDonationRequest request) {
        CampaignData campaign = campaignClient.getCampaign(request.campaignId());

        if (!STATUS_ACTIVE.equalsIgnoreCase(campaign.status())) {
            throw new InvalidDonationException("La campaña no está activa y no acepta donaciones");
        }

        return Donation.builder()
                .donorId(request.donorId())
                .campaignId(campaign.id())
                .ngoId(campaign.ngoId())
                .type(DonationType.CAMPAIGN)
                .amount(request.amount())
                .status(DonationStatus.PENDING)
                .build();
    }

    private String buildTitle(Donation donation) {
        return "Donación a campaña Paw+";
    }

    private BigDecimal creditedAmount(Donation donation) {
        return donation.getPaidAmount() != null ? donation.getPaidAmount() : donation.getAmount();
    }

    private String generateReceiptNumber(Donation donation) {
        String shortId = donation.getId().toString().substring(0, 8).toUpperCase();
        return "PAW-" + Year.now() + "-" + shortId;
    }

    private UUID parseDonationId(String externalReference) {
        try {
            return UUID.fromString(externalReference);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResourceNotFoundException("Referencia de donación inválida en el pago");
        }
    }

    private Donation getOrThrow(UUID id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donación no encontrada"));
    }

    private String fetchCampaignTitle(UUID campaignId) {
        if (campaignId == null) {
            return null;
        }

        try {
            return campaignClient.getCampaign(campaignId).title();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String fullName(UserData user) {
        if (user == null) {
            return null;
        }

        String first = user.firstName() != null ? user.firstName() : "";
        String last = user.lastName() != null ? user.lastName() : "";
        String name = (first + " " + last).trim();

        return name.isEmpty() ? null : name;
    }

    private DonationResponse toResponse(Donation d) {
        return new DonationResponse(
                d.getId(),
                d.getDonorId(),
                d.getCampaignId(),
                d.getNgoId(),
                d.getType(),
                d.getAmount(),
                d.getPaidAmount(),
                d.getStatus(),
                d.getPaymentMethod(),
                d.getReceiptNumber(),
                d.getCreatedAt(),
                d.getPaidAt()
        );
    }

    private DonationStatsResponse buildStats(List<Donation> donations, boolean personView) {
        long approved = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.APPROVED)
                .count();

        long pending = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.PENDING)
                .count();

        long rejected = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.REJECTED
                        || d.getStatus() == DonationStatus.EXPIRED
                        || d.getStatus() == DonationStatus.REFUNDED
                        || d.getStatus() == DonationStatus.CHARGED_BACK)
                .count();

        BigDecimal approvedTotal = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.APPROVED)
                .map(this::effectiveApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byMonth = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.APPROVED)
                .filter(d -> d.getPaidAt() != null || d.getCreatedAt() != null)
                .collect(
                        TreeMap::new,
                        (map, donation) -> {
                            String month = monthKey(donation);
                            map.merge(month, effectiveApprovedAmount(donation), BigDecimal::add);
                        },
                        TreeMap::putAll
                );

        return new DonationStatsResponse(
                personView ? approvedTotal : BigDecimal.ZERO,
                personView ? BigDecimal.ZERO : approvedTotal,
                approved,
                pending,
                rejected,
                new LinkedHashMap<>(byMonth)
        );
    }

    private BigDecimal effectiveApprovedAmount(Donation donation) {
        if (donation.getPaidAmount() != null) {
            return donation.getPaidAmount();
        }

        if (donation.getAmount() != null) {
            return donation.getAmount();
        }

        return BigDecimal.ZERO;
    }

    private String monthKey(Donation donation) {
        Instant date = donation.getPaidAt() != null ? donation.getPaidAt() : donation.getCreatedAt();

        return DateTimeFormatter.ofPattern("yyyy-MM")
                .withZone(ZoneId.systemDefault())
                .format(date);
    }

    private void notifyDonationCreated(Donation donation) {
        String metadata = buildMetadata(donation);

        notificationsClient.send(new NotificationEventRequest(
                "donations.created.donor." + donation.getId(),
                "DONATION_PENDING",
                Instant.now(),
                donation.getDonorId(),
                "NATURAL_PERSON",
                "Donación iniciada",
                "Tu donación fue iniciada y está pendiente de pago.",
                "DONATION",
                donation.getId(),
                "/persona/mis-donaciones",
                metadata
        ));

        notificationsClient.send(new NotificationEventRequest(
                "donations.created.ngo." + donation.getId(),
                "DONATION_CREATED",
                Instant.now(),
                donation.getNgoId(),
                "NGO",
                "Nueva donación iniciada",
                "Una persona inició una donación para una de tus campañas.",
                "DONATION",
                donation.getId(),
                "/ong/donaciones",
                metadata
        ));
    }

    private void notifyDonationStatusChanged(Donation donation, DonationStatus previousStatus, DonationStatus newStatus) {
        String metadata = buildMetadata(donation, previousStatus, newStatus);

        switch (newStatus) {
            case APPROVED -> {
                notificationsClient.send(new NotificationEventRequest(
                        "donations.approved.donor." + donation.getId(),
                        "DONATION_APPROVED",
                        Instant.now(),
                        donation.getDonorId(),
                        "NATURAL_PERSON",
                        "Donación aprobada",
                        "Tu donación fue aprobada correctamente. Ya puedes ver tu comprobante.",
                        "DONATION",
                        donation.getId(),
                        "/persona/mis-donaciones",
                        metadata
                ));

                notificationsClient.send(new NotificationEventRequest(
                        "donations.approved.ngo." + donation.getId(),
                        "DONATION_APPROVED",
                        Instant.now(),
                        donation.getNgoId(),
                        "NGO",
                        "Donación recibida",
                        "Una donación fue aprobada y sumada a tu campaña.",
                        "DONATION",
                        donation.getId(),
                        "/ong/donaciones",
                        metadata
                ));
            }
            case REJECTED, EXPIRED -> notificationsClient.send(new NotificationEventRequest(
                    "donations.rejected.donor." + donation.getId(),
                    "DONATION_REJECTED",
                    Instant.now(),
                    donation.getDonorId(),
                    "NATURAL_PERSON",
                    newStatus == DonationStatus.EXPIRED ? "Donación expirada" : "Donación rechazada",
                    newStatus == DonationStatus.EXPIRED
                            ? "Tu donación expiró porque el pago no fue completado."
                            : "Tu donación fue rechazada por el procesador de pago.",
                    "DONATION",
                    donation.getId(),
                    "/persona/mis-donaciones",
                    metadata
            ));
            case REFUNDED -> {
                notificationsClient.send(new NotificationEventRequest(
                        "donations.refunded.donor." + donation.getId(),
                        "DONATION_REFUNDED",
                        Instant.now(),
                        donation.getDonorId(),
                        "NATURAL_PERSON",
                        "Donación reembolsada",
                        "Tu donación fue reembolsada.",
                        "DONATION",
                        donation.getId(),
                        "/persona/mis-donaciones",
                        metadata
                ));

                notificationsClient.send(new NotificationEventRequest(
                        "donations.refunded.ngo." + donation.getId(),
                        "DONATION_REFUNDED",
                        Instant.now(),
                        donation.getNgoId(),
                        "NGO",
                        "Donación reembolsada",
                        "Una donación recibida fue reembolsada.",
                        "DONATION",
                        donation.getId(),
                        "/ong/donaciones",
                        metadata
                ));
            }
            case CHARGED_BACK -> {
                notificationsClient.send(new NotificationEventRequest(
                        "donations.charged_back.donor." + donation.getId(),
                        "DONATION_REJECTED",
                        Instant.now(),
                        donation.getDonorId(),
                        "NATURAL_PERSON",
                        "Donación revertida",
                        "Tu donación fue revertida por contracargo.",
                        "DONATION",
                        donation.getId(),
                        "/persona/mis-donaciones",
                        metadata
                ));

                notificationsClient.send(new NotificationEventRequest(
                        "donations.charged_back.ngo." + donation.getId(),
                        "DONATION_REJECTED",
                        Instant.now(),
                        donation.getNgoId(),
                        "NGO",
                        "Donación revertida",
                        "Una donación recibida fue revertida por contracargo.",
                        "DONATION",
                        donation.getId(),
                        "/ong/donaciones",
                        metadata
                ));
            }
            default -> {
                // No se notifica otro estado.
            }
        }
    }

    private String buildMetadata(Donation donation) {
        return buildMetadata(donation, null, donation.getStatus());
    }

    private String buildMetadata(Donation donation, DonationStatus previousStatus, DonationStatus newStatus) {
        return "{"
                + "\"donationId\":\"" + donation.getId() + "\","
                + "\"campaignId\":\"" + safeUuid(donation.getCampaignId()) + "\","
                + "\"ngoId\":\"" + safeUuid(donation.getNgoId()) + "\","
                + "\"donorId\":\"" + safeUuid(donation.getDonorId()) + "\","
                + "\"amount\":\"" + safeText(donation.getAmount()) + "\","
                + "\"previousStatus\":\"" + safeText(previousStatus) + "\","
                + "\"newStatus\":\"" + safeText(newStatus) + "\""
                + "}";
    }

    private String safeUuid(UUID value) {
        return value == null ? "" : value.toString();
    }

    private String safeText(Object value) {
        return value == null ? "" : value.toString().replace("\"", "'");
    }
}