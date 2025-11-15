-- Create members table
-- Stores pension member information

CREATE TABLE members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique member identifier',
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    date_of_birth DATE NOT NULL,
    employment_status VARCHAR(20) NOT NULL COMMENT 'ACTIVE, RETIRED, TERMINATED',
    department VARCHAR(100),
    position VARCHAR(100),
    hire_date DATE,
    retirement_date DATE,
    account_type VARCHAR(20) NOT NULL COMMENT 'DEFINED_BENEFIT, DEFINED_CONTRIBUTION',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, INACTIVE, SUSPENDED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_member_id (member_id),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_employment_status (employment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
