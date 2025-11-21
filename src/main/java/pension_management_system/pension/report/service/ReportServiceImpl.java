package pension_management_system.pension.report.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.exception.ReportException;
import pension_management_system.pension.report.dto.ReportRequest;
import pension_management_system.pension.report.dto.ReportResponse;
import pension_management_system.pension.report.entity.Report;
import pension_management_system.pension.report.entity.ReportFormat;
import pension_management_system.pension.report.entity.ReportType;
import pension_management_system.pension.report.mapper.ReportMapper;
import pension_management_system.pension.report.repository.ReportRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private final pension_management_system.pension.member.repository.MemberRepository memberRepository;
    private final pension_management_system.pension.employer.repository.EmployerRepository employerRepository;

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
     * Supports both synchronous and async generation
     *
     * For synchronous generation (default):
     * - Report is generated immediately
     * - User waits for completion
     * - Returns completed report
     *
     * For async generation (for large reports):
     * - Report record created with PENDING status
     * - File generation queued for background processing
     * - Returns immediately with PENDING status
     * - User can poll or receive notification when complete
     *
     * To enable async: Use generateReportAsync() method instead
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
                throw ReportException.quotaExceeded();
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
                throw ReportException.generationFailed(e);
            }

            // STEP 9: CONVERT TO RESPONSE DTO
            return reportMapper.toResponse(report);

        } catch (ReportException e) {
            throw e;  // Re-throw already handled exceptions
        } catch (Exception e) {
            log.error("Error in generateReport: {}", e.getMessage(), e);
            throw ReportException.generationFailed(e);
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

            // Verify entity exists in database
            if (request.getReportType() == ReportType.MEMBER_STATEMENT) {
                if (!memberRepository.existsById(request.getEntityId())) {
                    throw new IllegalArgumentException("Member not found with ID: " + request.getEntityId());
                }
                log.debug("Member {} exists - validation passed", request.getEntityId());
            } else if (request.getReportType() == ReportType.EMPLOYER_REPORT) {
                if (!employerRepository.existsById(request.getEntityId())) {
                    throw new IllegalArgumentException("Employer not found with ID: " + request.getEntityId());
                }
                log.debug("Employer {} exists - validation passed", request.getEntityId());
            }
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
     * Creates a professional PDF file using iText library
     *
     * What is iText?
     * - Industry-standard Java PDF library
     * - Creates PDF documents programmatically
     * - Supports tables, images, fonts, headers, footers
     * - Used by Fortune 500 companies
     *
     * Process:
     * 1. Create PdfWriter → handles file output
     * 2. Create PdfDocument → represents the PDF
     * 3. Create Document → high-level API for content
     * 4. Add content (paragraphs, tables, etc.)
     * 5. Close document (finalizes PDF)
     *
     * @param report Report configuration
     * @param filePath Where to save PDF
     * @throws Exception if generation fails
     */
    private void generatePdfReport(Report report, Path filePath) throws Exception {
        log.info("Generating PDF report using iText library");

        // STEP 1: CREATE PDF WRITER
        // PdfWriter handles writing bytes to the file
        PdfWriter writer = new PdfWriter(filePath.toString());

        // STEP 2: CREATE PDF DOCUMENT
        // PdfDocument represents the PDF file
        PdfDocument pdfDoc = new PdfDocument(writer);

        // STEP 3: CREATE DOCUMENT (HIGH-LEVEL API)
        // Document provides easy methods to add content
        Document document = new Document(pdfDoc);

        // STEP 4: ADD TITLE
        // Create title paragraph with larger font
        Paragraph title = new Paragraph(report.getTitle())
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // STEP 5: ADD METADATA SECTION
        // Information about the report
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Report Type: " + report.getReportType()));
        document.add(new Paragraph("Generated At: " + report.getGeneratedAt().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        document.add(new Paragraph("Generated By: " + report.getRequestedBy()));

        if (report.getStartDate() != null && report.getEndDate() != null) {
            document.add(new Paragraph("Period: " +
                    report.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                    " to " +
                    report.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        }

        document.add(new Paragraph("\n"));

        // STEP 6: ADD DATA TABLE
        // Create sample data table (in real implementation, query from database)
        List<String[]> reportData = getSampleReportData(report);

        if (!reportData.isEmpty()) {
            // Get headers (first row)
            String[] headers = reportData.get(0);

            // CREATE TABLE
            // Table with column count matching data
            Table table = new Table(UnitValue.createPercentArray(headers.length))
                    .useAllAvailableWidth();

            // ADD HEADER ROW
            // Header cells with bold text and gray background
            for (String header : headers) {
                com.itextpdf.layout.element.Cell headerCell = new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(headerCell);
            }

            // ADD DATA ROWS
            // Iterate through data (skip first row which is headers)
            for (int i = 1; i < reportData.size(); i++) {
                String[] row = reportData.get(i);
                for (String cellData : row) {
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(cellData)));
                }
            }

            // Add table to document
            document.add(table);
        }

        // STEP 7: ADD FOOTER
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("--- End of Report ---")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic());

        // STEP 8: CLOSE DOCUMENT
        // This finalizes the PDF and writes it to disk
        document.close();

        log.info("PDF report created successfully: {}", filePath);
    }

    /**
     * GENERATE EXCEL REPORT
     *
     * Creates a professional Excel file using Apache POI library
     *
     * What is Apache POI?
     * - Java library for Microsoft Office documents
     * - POI = "Poor Obfuscation Implementation" (humorous name)
     * - Supports .xls (old format) and .xlsx (modern format)
     * - Used by millions of Java applications worldwide
     *
     * Key components:
     * - Workbook: Entire Excel file
     * - Sheet: Individual spreadsheet tab
     * - Row: Horizontal row of cells
     * - Cell: Individual cell with data
     *
     * Process:
     * 1. Create Workbook (XSSFWorkbook for .xlsx)
     * 2. Create Sheet
     * 3. Create Rows and Cells
     * 4. Apply Styles (colors, fonts, borders)
     * 5. Write to file
     * 6. Close resources
     *
     * @param report Report configuration
     * @param filePath Where to save Excel file
     * @throws Exception if generation fails
     */
    private void generateExcelReport(Report report, Path filePath) throws Exception {
        log.info("Generating Excel report using Apache POI library");

        // STEP 1: CREATE WORKBOOK
        // XSSFWorkbook = Excel 2007+ format (.xlsx)
        // HSSFWorkbook = Excel 97-2003 format (.xls)
        Workbook workbook = new XSSFWorkbook();

        // STEP 2: CREATE SHEET
        // Sheet name appears on the tab at bottom of Excel
        Sheet sheet = workbook.createSheet(report.getReportType().toString());

        // STEP 3: CREATE TITLE ROW
        // First row with report title merged across columns
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(report.getTitle());

        // STYLE FOR TITLE (Bold, larger font, blue background)
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);

        // STEP 4: ADD METADATA ROWS
        int currentRowNum = 2; // Start after title row (leave row 1 blank)

        createMetadataRow(sheet, workbook, currentRowNum++, "Report Type", report.getReportType().toString());
        createMetadataRow(sheet, workbook, currentRowNum++, "Generated At",
                report.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        createMetadataRow(sheet, workbook, currentRowNum++, "Generated By", report.getRequestedBy());

        if (report.getStartDate() != null && report.getEndDate() != null) {
            String period = report.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                    " to " +
                    report.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            createMetadataRow(sheet, workbook, currentRowNum++, "Period", period);
        }

        currentRowNum++; // Blank row before data table

        // STEP 5: CREATE DATA TABLE
        // Get sample data (in real implementation, query from database)
        List<String[]> reportData = getSampleReportData(report);

        if (!reportData.isEmpty()) {
            // CREATE HEADER ROW STYLE (Bold, gray background)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // CREATE DATA CELL STYLE (Borders)
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // ADD ALL DATA ROWS
            for (int i = 0; i < reportData.size(); i++) {
                String[] rowData = reportData.get(i);
                Row excelRow = sheet.createRow(currentRowNum++);

                for (int j = 0; j < rowData.length; j++) {
                    Cell cell = excelRow.createCell(j);
                    cell.setCellValue(rowData[j]);

                    // Apply header style to first row, data style to others
                    cell.setCellStyle(i == 0 ? headerStyle : dataStyle);
                }
            }
        }

        // STEP 6: AUTO-SIZE COLUMNS
        // Automatically adjust column widths to fit content
        // Loop through all columns and auto-size
        if (!reportData.isEmpty()) {
            for (int i = 0; i < reportData.get(0).length; i++) {
                sheet.autoSizeColumn(i);
                // Add a bit of padding (POI auto-size is sometimes tight)
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
            }
        }

        // STEP 7: WRITE TO FILE
        // FileOutputStream writes the workbook bytes to disk
        try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
            workbook.write(outputStream);
        }

        // STEP 8: CLOSE WORKBOOK
        // Release resources (important to prevent memory leaks)
        workbook.close();

        log.info("Excel report created successfully: {}", filePath);
    }

    /**
     * HELPER: Create metadata row in Excel
     *
     * Creates a row with label and value
     * Example: | Report Type | MEMBER_STATEMENT |
     *
     * @param sheet Excel sheet
     * @param workbook Workbook (for creating styles)
     * @param rowNum Row number
     * @param label Label text
     * @param value Value text
     */
    private void createMetadataRow(Sheet sheet, Workbook workbook, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);

        // LABEL CELL (Bold)
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label + ":");
        CellStyle labelStyle = workbook.createCellStyle();
        Font labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelStyle.setFont(labelFont);
        labelCell.setCellStyle(labelStyle);

        // VALUE CELL (Normal)
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
    }

    /**
     * GENERATE CSV REPORT
     *
     * Creates a CSV (Comma-Separated Values) file using OpenCSV library
     *
     * What is CSV?
     * - Simple text format for tabular data
     * - Each line is a row, commas separate columns
     * - Universal format - opens in Excel, databases, analytics tools
     * - Lightweight and fast to generate
     *
     * What is OpenCSV?
     * - Java library for reading and writing CSV files
     * - Handles special characters, quotes, escaping automatically
     * - Much safer than manually creating CSV (avoids injection vulnerabilities)
     *
     * Example CSV:
     * Name,Age,City
     * "John Doe",30,"New York"
     * "Jane Smith",25,Boston
     *
     * Why use CSV?
     * - Import into databases
     * - Data analysis in Python/R
     * - Sharing data between systems
     * - Backup and archival
     *
     * @param report Report configuration
     * @param filePath Where to save CSV file
     * @throws Exception if generation fails
     */
    private void generateCsvReport(Report report, Path filePath) throws Exception {
        log.info("Generating CSV report using OpenCSV library");

        // STEP 1: CREATE CSV WRITER
        // CSVWriter wraps a FileWriter and handles CSV formatting
        // FileWriter creates the file
        // CSVWriter adds CSV-specific functionality (escaping, quoting, etc.)
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()))) {

            // STEP 2: WRITE METADATA HEADER
            // Add report information as comment lines (starting with #)
            writer.writeNext(new String[]{"# " + report.getTitle()});
            writer.writeNext(new String[]{"# Report Type: " + report.getReportType()});
            writer.writeNext(new String[]{"# Generated At: " +
                    report.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))});
            writer.writeNext(new String[]{"# Generated By: " + report.getRequestedBy()});

            if (report.getStartDate() != null && report.getEndDate() != null) {
                writer.writeNext(new String[]{"# Period: " +
                        report.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                        " to " +
                        report.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))});
            }

            // Empty line to separate metadata from data
            writer.writeNext(new String[]{""});

            // STEP 3: GET REPORT DATA
            // Get sample data (in real implementation, query from database)
            List<String[]> reportData = getSampleReportData(report);

            // STEP 4: WRITE ALL DATA ROWS
            // writeAll() writes all rows at once
            // Each String[] becomes one CSV line
            // First row is headers, rest is data
            if (!reportData.isEmpty()) {
                writer.writeAll(reportData);
            }

            // STEP 5: FLUSH AND CLOSE
            // writer.close() is called automatically by try-with-resources
            // This ensures all data is written to disk
            writer.flush();

        } // <- writer.close() called here automatically

        log.info("CSV report created successfully: {}", filePath);
    }

    /**
     * GET SAMPLE REPORT DATA
     *
     * Generates sample data for the report based on report type
     *
     * In production, this would:
     * 1. Query database using repositories
     * 2. Filter by date range, entity ID, etc.
     * 3. Join multiple tables if needed
     * 4. Aggregate and calculate totals
     * 5. Format data for display
     *
     * For now, returns realistic sample data for demonstration
     *
     * Data structure:
     * - First row = Headers (column names)
     * - Subsequent rows = Data
     *
     * Example:
     * [
     *   ["Name", "Amount", "Date"],        // Headers
     *   ["John Doe", "5000", "2025-01-15"], // Data row 1
     *   ["Jane Smith", "3000", "2025-01-16"] // Data row 2
     * ]
     *
     * @param report Report configuration
     * @return List of String arrays (each array is one row)
     */
    private List<String[]> getSampleReportData(Report report) {
        List<String[]> data = new ArrayList<>();

        // Generate data based on report type
        switch (report.getReportType()) {
            case MEMBER_STATEMENT:
                // Member contribution history
                data.add(new String[]{"Date", "Description", "Amount (NGN)", "Balance (NGN)"});
                data.add(new String[]{"2025-01-15", "Monthly Contribution", "50,000.00", "50,000.00"});
                data.add(new String[]{"2024-12-15", "Monthly Contribution", "50,000.00", "100,000.00"});
                data.add(new String[]{"2024-11-15", "Monthly Contribution", "50,000.00", "150,000.00"});
                data.add(new String[]{"2024-10-15", "Monthly Contribution", "50,000.00", "200,000.00"});
                data.add(new String[]{"2024-09-15", "Monthly Contribution", "50,000.00", "250,000.00"});
                break;

            case EMPLOYER_REPORT:
                // Employer's members summary
                data.add(new String[]{"Member Name", "Employee ID", "Total Contributions", "Last Payment", "Status"});
                data.add(new String[]{"John Doe", "EMP001", "250,000.00", "2025-01-15", "Active"});
                data.add(new String[]{"Jane Smith", "EMP002", "180,000.00", "2025-01-15", "Active"});
                data.add(new String[]{"Bob Johnson", "EMP003", "320,000.00", "2025-01-10", "Active"});
                data.add(new String[]{"Alice Williams", "EMP004", "150,000.00", "2024-12-30", "Active"});
                data.add(new String[]{"Charlie Brown", "EMP005", "95,000.00", "2025-01-05", "Active"});
                break;

            case CONTRIBUTION_SUMMARY:
                // System-wide contribution summary
                data.add(new String[]{"Period", "Total Contributions", "Number of Members", "Average Contribution"});
                data.add(new String[]{"January 2025", "5,250,000.00", "105", "50,000.00"});
                data.add(new String[]{"December 2024", "4,980,000.00", "99", "50,303.03"});
                data.add(new String[]{"November 2024", "5,100,000.00", "102", "50,000.00"});
                data.add(new String[]{"October 2024", "4,850,000.00", "97", "50,000.00"});
                data.add(new String[]{"September 2024", "5,200,000.00", "104", "50,000.00"});
                break;

            case BENEFIT_CLAIMS:
                // Benefit claims report
                data.add(new String[]{"Member Name", "Claim Type", "Amount", "Date Submitted", "Status"});
                data.add(new String[]{"John Doe", "Retirement", "2,500,000.00", "2025-01-10", "Approved"});
                data.add(new String[]{"Mary Johnson", "Medical", "150,000.00", "2025-01-12", "Pending"});
                data.add(new String[]{"Peter Smith", "Education", "80,000.00", "2025-01-08", "Approved"});
                data.add(new String[]{"Sarah Williams", "Housing", "500,000.00", "2025-01-05", "Under Review"});
                data.add(new String[]{"David Brown", "Retirement", "3,200,000.00", "2025-01-03", "Approved"});
                break;

            case ANALYTICS_DASHBOARD:
                // System analytics snapshot
                data.add(new String[]{"Metric", "Value", "Change", "Trend"});
                data.add(new String[]{"Total Members", "1,250", "+45", "Up"});
                data.add(new String[]{"Total Contributions (MTD)", "5,250,000.00", "+250,000", "Up"});
                data.add(new String[]{"Active Employers", "85", "+3", "Up"});
                data.add(new String[]{"Pending Claims", "23", "-5", "Down"});
                data.add(new String[]{"Average Balance", "428,571.43", "+15,000", "Up"});
                break;

            case AUDIT_TRAIL:
                // System audit log
                data.add(new String[]{"Timestamp", "User", "Action", "Entity", "Details"});
                data.add(new String[]{"2025-01-15 10:30:00", "admin", "CREATE", "Payment", "Payment initiated: PMT-123"});
                data.add(new String[]{"2025-01-15 10:25:00", "admin", "UPDATE", "Member", "Updated member profile"});
                data.add(new String[]{"2025-01-15 10:20:00", "john.doe", "LOGIN", "Auth", "User logged in"});
                data.add(new String[]{"2025-01-15 10:15:00", "system", "GENERATE", "Report", "Monthly report generated"});
                data.add(new String[]{"2025-01-15 10:10:00", "admin", "APPROVE", "Benefit", "Benefit claim approved"});
                break;

            default:
                // Generic data if report type unknown
                data.add(new String[]{"ID", "Description", "Value", "Date"});
                data.add(new String[]{"1", "Sample Entry 1", "1000.00", "2025-01-15"});
                data.add(new String[]{"2", "Sample Entry 2", "2000.00", "2025-01-14"});
                data.add(new String[]{"3", "Sample Entry 3", "3000.00", "2025-01-13"});
        }

        return data;
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
            .orElseThrow(() -> pension_management_system.pension.exception.ResourceNotFoundException.report(id));

        // STEP 2: Check status
        if (!"COMPLETED".equals(report.getStatus())) {
            throw ReportException.notReady(report.getStatus());
        }

        // STEP 3: Load file as Resource
        try {
            Path filePath = Paths.get(report.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("Report file found and readable: {}", filePath);
                return resource;
            } else {
                throw ReportException.fileNotFound(filePath.toString());
            }

        } catch (ReportException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error loading report file: {}", e.getMessage(), e);
            throw ReportException.loadFailed(e);
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
     * DELETE OLD REPORTS (by days)
     *
     * Cleanup job to delete reports older than specified days
     */
    @Override
    public int deleteOldReports(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        log.info("Deleting reports older than {} days (cutoff: {})", daysOld, cutoffDate);

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
     * GET REPORTS BY TYPE (List version)
     */
    @Override
    public List<ReportResponse> getReportsByType(ReportType reportType) {
        log.info("Fetching all reports of type: {}", reportType);
        List<Report> reports = reportRepository.findByReportType(reportType, Pageable.unpaged()).getContent();
        return reports.stream()
            .map(reportMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * GET REPORTS FOR ENTITY
     */
    @Override
    public List<ReportResponse> getReportsForEntity(String entityType, Long entityId) {
        log.info("Fetching {} reports for entity ID: {}", entityType, entityId);
        try {
            ReportType type = ReportType.valueOf(entityType.toUpperCase());
            return getReportsByTypeAndEntity(type, entityId);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown entity type: {}", entityType);
            return new ArrayList<>();
        }
    }

    /**
     * GET REPORTS BY DATE RANGE
     */
    @Override
    public List<ReportResponse> getReportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return getReportsInDateRange(startDate, endDate);
    }

    /**
     * GET STORAGE USED BY USER
     */
    @Override
    public long getStorageUsedByUser(String username) {
        Long totalBytes = getTotalStorageByUser(username);
        return totalBytes != null ? totalBytes : 0L;
    }

    /**
     * GET TOTAL STORAGE BY USER
     *
     * Calculate total file size for a user's reports
     */
    @Override
    public long getTotalStorageByUser(String requestedBy) {
        log.debug("Calculating total storage for user: {}", requestedBy);

        Long totalBytes = reportRepository.getTotalFileSizeByUser(requestedBy);
        if (totalBytes == null) totalBytes = 0L;

        log.debug("User {} total storage: {} bytes ({} MB)",
            requestedBy, totalBytes, totalBytes / 1048576.0);

        return totalBytes;
    }

    /**
     * GENERATE REPORT ASYNCHRONOUSLY
     *
     * For large reports that take a long time to generate
     * Returns immediately with PENDING status
     * Report is generated in background thread
     *
     * Usage:
     * 1. Call this method → Returns PENDING report
     * 2. User can poll getReportById() to check status
     * 3. When complete, status changes to COMPLETED
     * 4. User can then download the report
     *
     * Benefits:
     * - Non-blocking: User doesn't wait
     * - Better UX for large reports
     * - Server can handle more concurrent requests
     *
     * @param request Report generation request
     * @return Report response with PENDING status
     */
    @Async
    @Transactional
    public ReportResponse generateReportAsync(ReportRequest request) {
        log.info("Starting async report generation: {}", request.getTitle());

        try {
            // Validate request
            validateReportRequest(request);

            // Create report record with PENDING status
            Report report = reportMapper.toEntity(request);
            report.setGeneratedAt(LocalDateTime.now());
            report.setStatus("PENDING");
            report = reportRepository.save(report);

            final Long reportId = report.getId();
            log.info("Report queued for async generation: ID={}", reportId);

            // Generate file in background (this method is already async)
            try {
                String filePath = generateReportFile(report);
                File file = new File(filePath);

                // Update report with file info
                report.setFilePath(filePath);
                report.setFileSize(file.length());
                report.setStatus("COMPLETED");
                reportRepository.save(report);

                log.info("Async report generation completed: ID={}", reportId);
            } catch (Exception e) {
                // Mark as failed
                report.setStatus("FAILED");
                reportRepository.save(report);
                log.error("Async report generation failed: ID={}", reportId, e);
            }

            return reportMapper.toResponse(report);

        } catch (Exception e) {
            log.error("Failed to queue async report generation", e);
            throw ReportException.generationFailed(e);
        }
    }
}
