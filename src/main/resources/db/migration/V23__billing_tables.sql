-- V23: Billing and payment tables.

CREATE TABLE IF NOT EXISTS payment_transactions (
    id                        CHAR(36)      NOT NULL,
    created_at                DATETIME(6)   NOT NULL,
    updated_at                DATETIME(6)   NULL,
    created_by                CHAR(36)      NULL,
    updated_by                CHAR(36)      NULL,
    subscription_id           CHAR(36)      NULL,
    user_id                   CHAR(36)      NOT NULL,
    provider_code             VARCHAR(20)   NOT NULL,
    provider_reference        VARCHAR(200)  NULL,
    amount                    DECIMAL(10,2) NOT NULL,
    currency                  CHAR(3)       NOT NULL,
    status                    VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    initiated_at              DATETIME(6)   NOT NULL,
    completed_at              DATETIME(6)   NULL,
    raw_provider_response_json TEXT         NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_pt_ref (provider_reference),
    INDEX idx_pt_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_provider_events (
    id                CHAR(36)    NOT NULL,
    provider_code     VARCHAR(20) NOT NULL,
    event_type        VARCHAR(100)NULL,
    provider_event_id VARCHAR(200)NOT NULL,
    payload_json      TEXT        NULL,
    received_at       DATETIME(6) NOT NULL,
    processed_at      DATETIME(6) NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    PRIMARY KEY (id),
    UNIQUE KEY uq_ppe (provider_code, provider_event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS billing_invoices (
    id              CHAR(36)      NOT NULL,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NULL,
    created_by      CHAR(36)      NULL,
    updated_by      CHAR(36)      NULL,
    subscription_id CHAR(36)      NULL,
    user_id         CHAR(36)      NOT NULL,
    line_items_json TEXT          NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    currency        CHAR(3)       NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    issued_at       DATETIME(6)   NULL,
    paid_at         DATETIME(6)   NULL,
    invoice_number  VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_bi_number (invoice_number),
    INDEX idx_bi_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_attempts (
    id             CHAR(36)    NOT NULL,
    transaction_id CHAR(36)    NOT NULL,
    attempt_number INT         NOT NULL,
    attempted_at   DATETIME(6) NOT NULL,
    outcome        VARCHAR(20) NULL,
    error_code     VARCHAR(100)NULL,
    PRIMARY KEY (id),
    INDEX idx_pa_txn (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_reconciliations (
    id                 CHAR(36)    NOT NULL,
    transaction_id     CHAR(36)    NOT NULL,
    provider_reference VARCHAR(200)NULL,
    reconciled_at      DATETIME(6) NOT NULL,
    match_status       VARCHAR(20) NOT NULL DEFAULT 'MATCHED',
    PRIMARY KEY (id),
    INDEX idx_pr_txn (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoice_sequences (
    year      YEAR        NOT NULL,
    next_seq  BIGINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
