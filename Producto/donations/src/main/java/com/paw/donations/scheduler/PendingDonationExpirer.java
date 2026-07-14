package com.paw.donations.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.paw.donations.client.NotificationsClient;
import com.paw.donations.client.dto.NotificationEventRequest;
import com.paw.donations.enums.DonationStatus;
import com.paw.donations.model.Donation;
import com.paw.donations.repository.DonationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PendingDonationExpirer {

    private final DonationRepository donationRepository;
    private final NotificationsClient notificationsClient;

    @Value("${donations.pending-expiry-minutes:60}")
    private long expiryMinutes;

    @Scheduled(fixedDelayString = "${donations.expiry-check-ms:1800000}")
    @Transactional
    public void expireStalePending() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(expiryMinutes));
        List<Donation> stale = donationRepository.findByStatusAndCreatedAtBefore(DonationStatus.PENDING, cutoff);

        for (Donation donation : stale) {
            donation.setStatus(DonationStatus.EXPIRED);
            notifyDonationExpired(donation);
        }
    }

    private void notifyDonationExpired(Donation donation) {
        notificationsClient.send(new NotificationEventRequest(
                "donations.expired.donor." + donation.getId(),
                "DONATION_REJECTED",
                Instant.now(),
                donation.getDonorId(),
                "NATURAL_PERSON",
                "Donación expirada",
                "Tu donación expiró porque el pago no fue completado.",
                "DONATION",
                donation.getId(),
                "/persona/mis-donaciones",
                "{\"donationId\":\"" + donation.getId() + "\",\"status\":\"EXPIRED\"}"
        ));
    }
}