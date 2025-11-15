-- Create contributions table
-- Tracks member and employer contributions

CREATE TABLE contributions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique transaction reference',
    member_id BIGINT NOT NULL,
    employer_id BIGINT,
    contribution_type VARCHAR(20) NOT NULL COMMENT 'EMPLOYEE, EMPLOYER, VOLUNTARY',
    amount DECIMAL(15,2) NOT NULL,
    contribution_date DATE NOT NULL,
    payment_method VARCHAR(20) NOT NULL COMMENT 'BANK_TRANSFER, CARD, CASH, CHEQUE',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, COMPLETED, FAILED, REVERSED',
    reference_number VARCHAR(100),
    description TEXT,
    processed_by VARCHAR(100),
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE RESTRICT,
    FOREIGN KEY (employer_id) REFERENCES employers(id) ON DELETE SET NULL,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_member_id (member_id),
    INDEX idx_employer_id (employer_id),
    INDEX idx_contribution_date (contribution_date),
    INDEX idx_status (status),
    INDEX idx_contribution_type (contribution_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
