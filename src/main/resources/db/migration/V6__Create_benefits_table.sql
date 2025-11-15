-- Create benefits table
-- Tracks benefit claims and payouts

CREATE TABLE benefits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique claim reference',
    member_id BIGINT NOT NULL,
    benefit_type VARCHAR(30) NOT NULL COMMENT 'RETIREMENT, DEATH, DISABILITY, WITHDRAWAL',
    claim_amount DECIMAL(15,2) NOT NULL,
    approved_amount DECIMAL(15,2),
    claim_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, UNDER_REVIEW, APPROVED, REJECTED, PAID',
    reason TEXT,
    supporting_documents TEXT COMMENT 'JSON array of document URLs',
    reviewed_by VARCHAR(100),
    reviewed_at TIMESTAMP NULL,
    approved_by VARCHAR(100),
    approved_at TIMESTAMP NULL,
    payment_date DATE,
    payment_reference VARCHAR(100),
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE RESTRICT,
    INDEX idx_claim_id (claim_id),
    INDEX idx_member_id (member_id),
    INDEX idx_status (status),
    INDEX idx_benefit_type (benefit_type),
    INDEX idx_claim_date (claim_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
