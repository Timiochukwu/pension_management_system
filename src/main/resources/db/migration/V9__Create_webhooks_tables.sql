-- V9: Create webhooks tables for enterprise integrations

-- Webhooks table
CREATE TABLE webhooks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    secret VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    created_by VARCHAR(100),
    retry_count INT NOT NULL DEFAULT 3,
    timeout_seconds INT NOT NULL DEFAULT 30,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_triggered_at TIMESTAMP NULL,
    failure_count INT NOT NULL DEFAULT 0,
    INDEX idx_webhook_url (url),
    INDEX idx_webhook_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Webhook events subscription table
CREATE TABLE webhook_events (
    webhook_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    FOREIGN KEY (webhook_id) REFERENCES webhooks(id) ON DELETE CASCADE,
    INDEX idx_webhook_events (webhook_id, event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Webhook deliveries log table
CREATE TABLE webhook_deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    webhook_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT,
    response_status INT,
    response_body TEXT,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    attempt_count INT NOT NULL DEFAULT 1,
    duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    next_retry_at TIMESTAMP NULL,
    FOREIGN KEY (webhook_id) REFERENCES webhooks(id) ON DELETE CASCADE,
    INDEX idx_delivery_webhook (webhook_id),
    INDEX idx_delivery_status (status),
    INDEX idx_delivery_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
