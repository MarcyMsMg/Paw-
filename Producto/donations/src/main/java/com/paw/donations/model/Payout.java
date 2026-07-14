package com.paw.donations.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Liquidación: registro de una transferencia que el admin YA hizo a una ONG.
// El sistema no mueve plata; solo registra el pago para llevar la cuenta de cuánto
// se le ha pagado a cada ONG. Cada Payout descuenta del saldo pendiente.
@Entity
@Table(name = "payouts", schema = "donations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    // Comprobante / número de la transferencia bancaria o de MercadoPago.
    @Column(name = "reference", length = 200)
    private String reference;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
