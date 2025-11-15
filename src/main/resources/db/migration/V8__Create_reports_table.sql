-- Create reports table
-- Stores generated reports and their metadata

CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique report identifier',
    report_name VARCHAR(200) NOT NULL,
    report_type VARCHAR(30) NOT NULL COMMENT 'MEMBER_SUMMARY, CONTRIBUTION_REPORT, BENEFIT_CLAIMS, PAYMENT_HISTORY',
    format VARCHAR(10) NOT NULL COMMENT 'PDF, EXCEL, CSV',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, GENERATING, COMPLETED, FAILED',
    file_path VARCHAR(500) COMMENT 'Path to generated report file',
    file_size BIGINT COMMENT 'File size in bytes',
    parameters TEXT COMMENT 'JSON report parameters (date range, filters, etc)',
    generated_by VARCHAR(100) NOT NULL,
    generated_at TIMESTAMP NULL,
    error_message TEXT,
    download_count INT DEFAULT 0,
    last_downloaded_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL COMMENT 'Report expiration date',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_report_id (report_id),
    INDEX idx_report_type (report_type),
    INDEX idx_status (status),
    INDEX idx_generated_by (generated_by),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
