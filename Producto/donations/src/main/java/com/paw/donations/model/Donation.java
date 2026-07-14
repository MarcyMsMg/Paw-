package com.paw.donations.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.paw.donations.enums.DonationStatus;
import com.paw.donations.enums.DonationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "donations", schema = "donations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Referencias lógicas a otros microservicios (solo guardamos los UUID).
    @Column(name = "donor_id", nullable = false)
    private UUID donorId;

    // Nulo cuando es un aporte directo a la ONG (sin campaña).
    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private DonationType type;

    // Monto solicitado por el donante al iniciar la donación.
    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    // Monto realmente cobrado según MercadoPago (transaction_amount = BRUTO). Se setea
    // al aprobarse el pago y es el que se acredita al recaudado de la campaña.
    @Column(name = "paid_amount", precision = 14, scale = 2)
    private BigDecimal paidAmount;

    // Monto NETO que MercadoPago acredita a nuestra cuenta (net_received_amount =
    // bruto − comisión de MercadoPago). Es lo que realmente se le puede transferir a
    // la ONG, así que el saldo liquidable se calcula con este valor.
    @Column(name = "net_amount", precision = 14, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DonationStatus status;

    // Método de pago tal cual lo reporta MercadoPago (ej. credit_card, account_money).
    @Column(name = "payment_method", length = 60)
    private String paymentMethod;

    // Número de comprobante, generado cuando el pago se aprueba.
    @Column(name = "receipt_number", length = 40)
    private String receiptNumber;

    // Referencias del lado de MercadoPago.
    @Column(name = "preference_id", length = 120)
    private String preferenceId;

    @Column(name = "external_payment_id", length = 120)
    private String externalPaymentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    // Bloqueo optimista: evita que dos webhooks concurrentes procesen el mismo
    // pago a la vez (que duplicarían el monto acreditado).
    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = DonationStatus.PENDING;
        }
    }
}
