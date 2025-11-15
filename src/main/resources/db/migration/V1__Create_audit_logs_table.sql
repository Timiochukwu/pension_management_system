-- Flyway Migration V1: Create audit_logs table
--
-- Purpose: Database version control
--
-- Flyway automatically executes SQL files in order
-- Filename format: V{version}__{description}.sql
--
-- Benefits:
-- - Track database changes in version control
-- - Automated migrations on deployment
-- - Rollback support
-- - Team collaboration
--
-- Run with: mvn flyway:migrate

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,

    INDEX idx_user_email (user_email),
    INDEX idx_timestamp (timestamp),
    INDEX idx_entity_type (entity_type),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
