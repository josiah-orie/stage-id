package com.jovine360.talenthub.billing.domain.model;

import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "billing_invoices")
@Getter
@Setter
public class BillingInvoice extends AuditableEntity {

    @Column(name = "subscription_id", columnDefinition = "CHAR(36)")
    private String subscriptionId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String userId;

    @Column(name = "line_items_json", columnDefinition = "TEXT")
    private String lineItemsJson;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "currency", columnDefinition = "CHAR(3)", nullable = false)
    private String currency;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "DRAFT"; // DRAFT / ISSUED / PAID / VOID

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "invoice_number", length = 50, unique = true)
    private String invoiceNumber; // INV-{YEAR}-{SEQ}
}
