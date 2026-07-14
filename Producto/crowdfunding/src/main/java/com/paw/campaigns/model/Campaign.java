package com.paw.campaigns.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.paw.campaigns.enums.CampaignStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "campaigns", schema = "campaigns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Referencia logica a la ONG dueña de la campaña. No duplicamos los datos de la
    // ONG: solo guardamos su id. Los datos viven en el microservicio de Usuarios.
    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "banner_url", length = 1000)
    private String bannerUrl;

    // Link de YouTube opcional con el video de presentación de la campaña.
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(length = 80)
    private String category;

    @Column(name = "goal_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal goalAmount;

    @Column(name = "raised_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal raisedAmount;

    // Fechas programadas de la campaña. Antes de startDate la campaña no aparece
    // públicamente; después de endDate deja de listarse en crowdfunding.
    // Nullable a nivel de BD para no romper filas existentes; obligatorias en el
    // request de creación (validación en el DTO).
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = CampaignStatus.ACTIVE;
        }
        if (this.raisedAmount == null) {
            this.raisedAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
