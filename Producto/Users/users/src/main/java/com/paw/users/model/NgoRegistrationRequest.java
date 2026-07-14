package com.paw.users.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.paw.users.enums.NgoRegistrationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ngo_registration_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NgoRegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "ngo_name", nullable = false, length = 150)
    private String ngoName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "constitution_act_url", columnDefinition = "TEXT")
    private String constitutionActUrl;

    @Column(length = 120)
    private String location;

    @Column(name = "foundation_year")
    private Integer foundationYear;

    @Column(name = "rescued_animals_count")
    private Integer rescuedAnimalsCount;

    @Column(name = "volunteers_count")
    private Integer volunteersCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NgoRegistrationStatus status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = NgoRegistrationStatus.PENDING;
        }
    }
}