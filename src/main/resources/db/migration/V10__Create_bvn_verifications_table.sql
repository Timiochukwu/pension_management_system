-- V10: Create BVN verification table for Nigerian market compliance

CREATE TABLE bvn_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL UNIQUE,
    bvn_number VARCHAR(11) NOT NULL,
    status VARCHAR(20) NOT NULL,
    verified_first_name VARCHAR(100),
    verified_last_name VARCHAR(100),
    verified_date_of_birth DATE,
    verified_phone_number VARCHAR(20),
    verified_gender VARCHAR(10),
    match_score INT,
    verification_date TIMESTAMP NULL,
    provider VARCHAR(50),
    provider_reference VARCHAR(100),
    error_message TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    INDEX idx_bvn_member (member_id),
    INDEX idx_bvn_number (bvn_number),
    INDEX idx_bvn_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
