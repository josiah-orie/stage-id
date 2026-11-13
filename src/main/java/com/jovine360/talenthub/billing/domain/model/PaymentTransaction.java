package com.jovine360.talenthub.billing.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
public class PaymentTransaction extends AuditableEntity {

    @Column(name = "subscription_id", columnDefinition = "CHAR(36)")
    private String subscriptionId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String userId;

    @Column(name = "provider_code", length = 20, nullable = false)
    private String providerCode;

    @Column(name = "provider_reference", length = 200, unique = true)
    private String providerReference;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", columnDefinition = "CHAR(3)", nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "initiated_at", nullable = false)
    private Instant initiatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "raw_provider_response_json", columnDefinition = "TEXT")
    private String rawProviderResponseJson;
}
