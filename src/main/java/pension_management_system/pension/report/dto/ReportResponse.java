package pension_management_system.pension.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.report.entity.ReportFormat;
import pension_management_system.pension.report.entity.ReportType;

import java.time.LocalDateTime;

/**
 * Response DTO for report operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String title;
    private ReportType reportType;
    private ReportFormat format;
    private String status;
    private String filePath;
    private Long fileSize;
    private Long entityId;
    private String entityType;
    private String requestedBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
}
