package pension_management_system.pension.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Report entity for storing generated report metadata
 */
@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_report_type", columnList = "reportType"),
        @Index(name = "idx_requested_by", columnList = "requestedBy"),
        @Index(name = "idx_generated_at", columnList = "generatedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportFormat format = ReportFormat.PDF;

    @Column(length = 50)
    @Builder.Default
    private String status = "PENDING";

    @Column(nullable = false, length = 255)
    private String requestedBy;

    @Column
    private LocalDateTime generatedAt;

    @Column
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Column
    private Long entityId;

    @Column(length = 500)
    private String filePath;

    @Column
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * Mark report as completed
     */
    public void markAsCompleted(String filePath, Long fileSize) {
        this.status = "COMPLETED";
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    /**
     * Mark report as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }
}
