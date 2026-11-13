package com.jovine360.talenthub.billing.domain.model;

import com.jovine360.talenthub.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "payment_provider_events",
       uniqueConstraints = @UniqueConstraint(columnNames = {"provider_code", "provider_event_id"}))
@Getter
@Setter
public class PaymentProviderEvent extends BaseEntity {

    @Column(name = "provider_code", length = 20, nullable = false)
    private String providerCode;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "provider_event_id", length = 200, nullable = false)
    private String providerEventId;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "RECEIVED"; // RECEIVED / PROCESSED / DUPLICATE / FAILED
}
