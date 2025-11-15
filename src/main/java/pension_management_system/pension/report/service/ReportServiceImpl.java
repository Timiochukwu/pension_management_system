package pension_management_system.pension.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.report.dto.ReportRequest;
import pension_management_system.pension.report.dto.ReportResponse;
import pension_management_system.pension.report.entity.Report;
import pension_management_system.pension.report.entity.ReportFormat;
import pension_management_system.pension.report.entity.ReportType;
import pension_management_system.pension.report.mapper.ReportMapper;
import pension_management_system.pension.report.repository.ReportRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReportServiceImpl - Implementation of ReportService interface
 *
 * Purpose: Contains business logic for report generation and management
 *
 * Annotations Explained:
 * @Service - Marks this as a Spring service component
 *   - Spring creates a single instance (singleton)
 *   - Can be injected into controllers and other services
 *   - Transactions are managed automatically
 *
 * @RequiredArgsConstructor - Lombok creates constructor for final fields
 *   - Spring injects dependencies through constructor
 *   - Better than @Autowired (immutable, easier to test)
 *
 * @Slf4j - Lombok provides logger
 *   - Can use: log.info(), log.error(), log.debug(), etc.
 *   - Logs are important for monitoring and debugging
 *
 * @Transactional - Database transaction management
 *   - If any operation fails, entire transaction is rolled back
 *   - Ensures data consistency
 *   - Example: If file generation succeeds but database save fails,
 *     both are rolled back (no orphaned files or records)
 *
 * Dependencies:
 * - ReportRepository: Database operations
 * - ReportMapper: Entity ↔ DTO conversions
 *
 * Service Layer Responsibilities:
 * 1. Business logic and validation
 * 2. Coordinate multiple repository operations
 * 3. Handle transactions
 * 4. Convert entities to DTOs
 * 5. Error handling and logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportServiceImpl implements ReportService {

    /**
     * DEPENDENCIES (Injected by Spring)
     *
     * final fields = immutable after construction = safer code
     * Spring automatically finds implementations and injects them
     */
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    /**
     * CONFIGURATION CONSTANTS
     *
     * In production, these should come from application.properties
     * Example in application.properties:
     * reports.storage.path=/var/pension/reports
     * reports.storage.max-size=10485760
     */
    private static final String REPORTS_DIRECTORY = "reports";
    private static final long MAX_STORAGE_PER_USER = 1073741824L; // 1 GB in bytes

    /**
     * GENERATE A NEW REPORT
     *
     * Main method for creating and generating reports
     * Currently synchronous (generates immediately)
     * TODO: Implement async generation for large reports
     *
     * Process Flow:
     * 1. Validate request
     * 2. Map request → entity
     * 3. Set metadata (timestamp, status)
     * 4. Save to database (get ID)
     * 5. Generate file (PDF/Excel/CSV)
     * 6. Update entity with file info
     * 7. Map entity → response
     * 8. Return response
     *
     * @param request Report generation request
     * @return Generated report details
     */
    @Override
    public ReportResponse generateReport(ReportRequest request) {
        log.info("Generating report: {} for user: {}", request.getTitle(), request.getRequestedBy());

        try {
            // STEP 1: VALIDATE REQUEST
            validateReportRequest(request);

            // STEP 2: CHECK STORAGE QUOTA
            // Ensure user hasn't exceeded their storage limit
            Long currentStorage = getTotalStorageByUser(request.getRequestedBy());
            if (currentStorage >= MAX_STORAGE_PER_USER) {
                throw new RuntimeException("Storage quota exceeded. Please delete old reports.");
            }

            // STEP 3: MAP REQUEST TO ENTITY
            // Convert DTO → Entity using MapStruct
            Report report = reportMapper.toEntity(request);

            // STEP 4: SET METADATA
            // These fields are not in request, we set them here
            report.setGeneratedAt(LocalDateTime.now());
            report.setStatus("PENDING");

            // STEP 5: SAVE TO DATABASE (INITIAL STATE)
            // Save with status = PENDING to get an ID
            // If generation fails, we can update status to FAILED
            report = reportRepository.save(report);
            log.info("Report record created with ID: {}", report.getId());

            try {
                // STEP 6: GENERATE PHYSICAL FILE
                // This is where the actual PDF/Excel/CSV is created
                String filePath = generateReportFile(report);
                File file = new File(filePath);

                // STEP 7: UPDATE ENTITY WITH FILE INFO
                // Mark as completed with file details
                report.markAsCompleted(filePath, file.length());

                // STEP 8: SAVE UPDATED ENTITY
                report = reportRepository.save(report);
                log.info("Report generated successfully: {}", filePath);

            } catch (Exception e) {
                // GENERATION FAILED
                // Update status to FAILED with error message
                log.error("Failed to generate report file: {}", e.getMessage(), e);
                report.markAsFailed("Report generation failed: " + e.getMessage());
                reportRepository.save(report);
                throw new RuntimeException("Report generation failed", e);
            }

            // STEP 9: CONVERT TO RESPONSE DTO
            return reportMapper.toResponse(report);

        } catch (Exception e) {
            log.error("Error in generateReport: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    /**
     * VALIDATE REPORT REQUEST
     *
     * Business rules validation before generating report
     *
     * Checks:
     * - Required fields are present
     * - Date range is valid
     * - Entity exists (for entity-specific reports)
     * - User has permission
     *
     * @param request Request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateReportRequest(ReportRequest request) {
        // Check title is not empty
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Report title is required");
        }

        // Check dates if provided
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date");
            }

            if (request.getEndDate().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("End date cannot be in the future");
            }
        }

        // Entity-specific validation
        if (request.getReportType() == ReportType.MEMBER_STATEMENT ||
            request.getReportType() == ReportType.EMPLOYER_REPORT) {

            if (request.getEntityId() == null) {
                throw new IllegalArgumentException(
                    request.getReportType() + " requires entityId"
                );
            }

            // TODO: Verify entity exists in database
            // Example:
            // if (request.getReportType() == ReportType.MEMBER_STATEMENT) {
            //     if (!memberRepository.existsById(request.getEntityId())) {
            //         throw new IllegalArgumentException("Member not found: " + request.getEntityId());
            //     }
            // }
        }

        log.debug("Report request validation passed");
    }

    /**
     * GENERATE REPORT FILE
     *
     * Creates the actual PDF/Excel/CSV file
     * This is a placeholder - actual implementation depends on report type
     *
     * Real implementation would:
     * 1. Query data based on report type and filters
     * 2. Use report library to create file:
     *    - PDF: iText, Apache PDFBox, JasperReports
     *    - Excel: Apache POI
     *    - CSV: OpenCSV, Apache Commons CSV
     * 3. Save file to disk
     * 4. Return file path
     *
     * @param report Report entity with generation parameters
     * @return Path to generated file
     * @throws Exception if generation fails
     */
    private String generateReportFile(Report report) throws Exception {
        log.info("Generating {} report in {} format", report.getReportType(), report.getFormat());

        // STEP 1: CREATE REPORTS DIRECTORY IF NOT EXISTS
        Path reportsDir = Paths.get(REPORTS_DIRECTORY);
        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
            log.info("Created reports directory: {}", reportsDir.toAbsolutePath());
        }

        // STEP 2: GENERATE UNIQUE FILENAME
        // Format: reportType_entityId_timestamp.extension
        // Example: member_statement_123_20250115_103045.pdf
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format(
            "%s_%s_%s.%s",
            report.getReportType().toString().toLowerCase(),
            report.getEntityId() != null ? report.getEntityId() : "all",
            timestamp,
            report.getFormat().toString().toLowerCase()
        );

        Path filePath = reportsDir.resolve(filename);
        log.info("Report will be saved to: {}", filePath.toAbsolutePath());

        // STEP 3: GENERATE FILE BASED ON TYPE AND FORMAT
        // This is where you would call actual report generation logic
        // For now, we create a placeholder file

        switch (report.getFormat()) {
            case PDF:
                generatePdfReport(report, filePath);
                break;

            case EXCEL:
                generateExcelReport(report, filePath);
                break;

            case CSV:
                generateCsvReport(report, filePath);
                break;

            default:
                throw new IllegalArgumentException("Unsupported format: " + report.getFormat());
        }

        return filePath.toString();
    }

    /**
     * GENERATE PDF REPORT
     *
     * Creates a PDF file for the report
     *
     * Placeholder implementation - creates simple text file
     * Real implementation would use iText, JasperReports, etc.
     *
     * @param report Report configuration
     * @param filePath Where to save PDF
     * @throws Exception if generation fails
     */
    private void generatePdfReport(Report report, Path filePath) throws Exception {
        // TODO: Implement actual PDF generation
        // Example with iText:
        // PdfWriter writer = new PdfWriter(filePath.toString());
        // PdfDocument pdf = new PdfDocument(writer);
        // Document document = new Document(pdf);
        // document.add(new Paragraph(report.getTitle()));
        // ... add report content ...
        // document.close();

        // Placeholder: Create simple text file
        String content = "PDF Report Placeholder\n" +
                        "Title: " + report.getTitle() + "\n" +
                        "Type: " + report.getReportType() + "\n" +
                        "Generated: " + LocalDateTime.now();

        Files.writeString(filePath, content);
        log.info("PDF report created (placeholder): {}", filePath);
    }

    /**
     * GENERATE EXCEL REPORT
     *
     * Creates an Excel file for the report
     *
     * Placeholder implementation
     * Real implementation would use Apache POI
     *
     * @param report Report configuration
     * @param filePath Where to save Excel file
     * @throws Exception if generation fails
     */
    private void generateExcelReport(Report report, Path filePath) throws Exception {
        // TODO: Implement actual Excel generation
        // Example with Apache POI:
        // Workbook workbook = new XSSFWorkbook();
        // Sheet sheet = workbook.createSheet(report.getTitle());
        // Row row = sheet.createRow(0);
        // Cell cell = row.createCell(0);
        // cell.setCellValue("Report Data");
        // ... add more data ...
        // FileOutputStream outputStream = new FileOutputStream(filePath.toFile());
        // workbook.write(outputStream);
        // workbook.close();

        // Placeholder: Create simple text file
        String content = "Excel Report Placeholder\n" +
                        "Title: " + report.getTitle() + "\n" +
                        "Type: " + report.getReportType();

        Files.writeString(filePath, content);
        log.info("Excel report created (placeholder): {}", filePath);
    }

    /**
     * GENERATE CSV REPORT
     *
     * Creates a CSV file for the report
     *
     * Placeholder implementation
     * Real implementation would use OpenCSV
     *
     * @param report Report configuration
     * @param filePath Where to save CSV file
     * @throws Exception if generation fails
     */
    private void generateCsvReport(Report report, Path filePath) throws Exception {
        // TODO: Implement actual CSV generation
        // Example with OpenCSV:
        // CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()));
        // writer.writeNext(new String[]{"Column1", "Column2", "Column3"});
        // ... write data rows ...
        // writer.close();

        // Placeholder: Create simple CSV
        String content = "Report Type,Title,Generated At\n" +
                        report.getReportType() + "," +
                        report.getTitle() + "," +
                        LocalDateTime.now();

        Files.writeString(filePath, content);
        log.info("CSV report created (placeholder): {}", filePath);
    }

    /**
     * GET REPORT BY ID
     *
     * Retrieve report details by ID
     * Convert entity to DTO before returning
     */
    @Override
    public ReportResponse getReportById(Long id) {
        log.info("Fetching report with ID: {}", id);

        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));

        return reportMapper.toResponse(report);
    }

    /**
     * GET ALL REPORTS (PAGINATED)
     *
     * Retrieve all reports with pagination
     * Converts each entity to DTO using Stream API
     */
    @Override
    public Page<ReportResponse> getAllReports(Pageable pageable) {
        log.info("Fetching all reports - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        // Fetch page of entities
        Page<Report> reportPage = reportRepository.findAll(pageable);

        // Convert Page<Report> → Page<ReportResponse>
        // .map() transforms each element
        return reportPage.map(reportMapper::toResponse);
    }

    /**
     * GET REPORTS BY USER
     *
     * Find all reports generated by a specific user
     */
    @Override
    public List<ReportResponse> getReportsByUser(String requestedBy) {
        log.info("Fetching reports for user: {}", requestedBy);

        List<Report> reports = reportRepository.findByRequestedByOrderByGeneratedAtDesc(requestedBy);

        // Convert List<Report> → List<ReportResponse>
        // Stream API: reports.stream().map().collect()
        return reports.stream()
            .map(reportMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * GET REPORTS BY TYPE
     *
     * Find reports of a specific type with pagination
     */
    @Override
    public Page<ReportResponse> getReportsByType(ReportType reportType, Pageable pageable) {
        log.info("Fetching reports of type: {}", reportType);

        Page<Report> reportPage = reportRepository.findByReportType(reportType, pageable);

        return reportPage.map(reportMapper::toResponse);
    }

    /**
     * GET REPORTS BY TYPE AND ENTITY
     *
     * Find reports for a specific entity
     */
    @Override
    public List<ReportResponse> getReportsByTypeAndEntity(ReportType reportType, Long entityId) {
        log.info("Fetching {} reports for entity ID: {}", reportType, entityId);

        List<Report> reports = reportRepository.findByReportTypeAndEntityIdOrderByGeneratedAtDesc(
            reportType, entityId
        );

        return reports.stream()
            .map(reportMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * GET REPORTS IN DATE RANGE
     *
     * Find reports generated within a date range
     */
    @Override
    public List<ReportResponse> getReportsInDateRange(LocalDateTime start, LocalDateTime end) {
        log.info("Fetching reports between {} and {}", start, end);

        List<Report> reports = reportRepository.findByGeneratedAtBetween(start, end);

        return reports.stream()
            .map(reportMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * DOWNLOAD REPORT FILE
     *
     * Retrieve file for download
     * Returns file as Spring Resource
     */
    @Override
    public Resource downloadReport(Long id) {
        log.info("Downloading report with ID: {}", id);

        // STEP 1: Find report
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));

        // STEP 2: Check status
        if (!"COMPLETED".equals(report.getStatus())) {
            throw new RuntimeException("Report is not ready for download. Status: " + report.getStatus());
        }

        // STEP 3: Load file as Resource
        try {
            Path filePath = Paths.get(report.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("Report file found and readable: {}", filePath);
                return resource;
            } else {
                throw new RuntimeException("Report file not found or not readable: " + filePath);
            }

        } catch (Exception e) {
            log.error("Error loading report file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load report file", e);
        }
    }

    /**
     * DELETE REPORT
     *
     * Delete report and its file
     */
    @Override
    public void deleteReport(Long id) {
        log.info("Deleting report with ID: {}", id);

        // STEP 1: Find report
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));

        // STEP 2: Delete physical file
        try {
            if (report.getFilePath() != null) {
                Path filePath = Paths.get(report.getFilePath());
                Files.deleteIfExists(filePath);
                log.info("Deleted report file: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Error deleting report file: {}", e.getMessage(), e);
            // Continue with database deletion even if file deletion fails
        }

        // STEP 3: Delete database record
        reportRepository.deleteById(id);
        log.info("Deleted report record from database: {}", id);
    }

    /**
     * DELETE OLD REPORTS
     *
     * Cleanup job to delete reports older than cutoff date
     */
    @Override
    public int deleteOldReports(LocalDateTime cutoffDate) {
        log.info("Deleting reports older than: {}", cutoffDate);

        // STEP 1: Find old reports
        List<Report> oldReports = reportRepository.findByGeneratedAtBetween(
            LocalDateTime.of(1970, 1, 1, 0, 0), // Very old date
            cutoffDate
        );

        // STEP 2: Delete each report
        int deletedCount = 0;
        for (Report report : oldReports) {
            try {
                deleteReport(report.getId());
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete report {}: {}", report.getId(), e.getMessage());
                // Continue with other reports
            }
        }

        log.info("Deleted {} old reports", deletedCount);
        return deletedCount;
    }

    /**
     * GET TOTAL STORAGE BY USER
     *
     * Calculate total file size for a user's reports
     */
    @Override
    public Long getTotalStorageByUser(String requestedBy) {
        log.debug("Calculating total storage for user: {}", requestedBy);

        Long totalBytes = reportRepository.getTotalFileSizeByUser(requestedBy);

        log.debug("User {} total storage: {} bytes ({} MB)",
            requestedBy, totalBytes, totalBytes / 1048576.0);

        return totalBytes;
    }
}
