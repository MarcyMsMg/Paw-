package com.paw.donations.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

// Datos bancarios de una ONG para recibir las transferencias (liquidaciones).
// Una ONG tiene a lo más una cuenta (ngoId único). Los administra la propia ONG
// desde su perfil; el admin los usa al hacer las transferencias.
@Entity
@Table(name = "payout_accounts", schema = "donations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ngo_id", nullable = false, unique = true)
    private UUID ngoId;

    @Column(name = "holder_name", length = 160)
    private String holderName;

    @Column(name = "rut", length = 20)
    private String rut;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "account_type", length = 40)
    private String accountType;

    @Column(name = "account_number", length = 60)
    private String accountNumber;

    @Column(name = "email", length = 160)
    private String email;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.updatedAt = Instant.now();
    }
}
