package pension_management_system.pension.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pension_management_system.pension.report.entity.ReportFormat;
import pension_management_system.pension.report.entity.ReportType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Request DTO for generating a new report
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    @NotBlank(message = "Report title is required")
    private String title;

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    @NotNull(message = "Report format is required")
    private ReportFormat format;

    private Long entityId;
    private String entityType;

    @NotBlank(message = "Requested by is required")
    private String requestedBy;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Map<String, Object> parameters;
}
