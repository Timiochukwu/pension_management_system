-- Create employers table
-- Stores employer/company information

CREATE TABLE employers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employer_id VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique employer identifier',
    company_name VARCHAR(200) NOT NULL,
    industry VARCHAR(100),
    tax_id VARCHAR(50) UNIQUE,
    registration_number VARCHAR(50),
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'Nigeria',
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    contribution_rate DECIMAL(5,2) COMMENT 'Employer contribution percentage',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_employer_id (employer_id),
    INDEX idx_company_name (company_name),
    INDEX idx_tax_id (tax_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
