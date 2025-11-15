package pension_management_system.pension.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.analytics.dto.SystemStatisticsDto;
import pension_management_system.pension.analytics.service.AnalyticsService;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.service.ContributionService;
import pension_management_system.pension.member.dto.MemberResponse;
import pension_management_system.pension.member.service.MemberService;
import pension_management_system.pension.report.dto.ExportFormat;
import pension_management_system.pension.report.service.CsvExportService;
import pension_management_system.pension.report.service.ExcelExportService;
import pension_management_system.pension.report.service.PdfExportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Report Generation and Export APIs")
public class ReportController {

    private final MemberService memberService;
    private final ContributionService contributionService;
    private final AnalyticsService analyticsService;
    private final CsvExportService csvExportService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    @GetMapping("/members/export")
    @Operation(summary = "Export all members report")
    public ResponseEntity<byte[]> exportMembers(@RequestParam ExportFormat format) {
        log.info("GET /api/v1/reports/members/export - Export members as {}", format);

        List<MemberResponse> members = memberService.getAllActiveMembers();
        byte[] data;
        String filename;
        MediaType mediaType;

        switch (format) {
            case CSV:
                data = csvExportService.exportMembersToCSV(members);
                filename = generateFilename("members", "csv");
                mediaType = MediaType.parseMediaType("text/csv");
                break;

            case EXCEL:
                data = excelExportService.exportMembersToExcel(members);
                filename = generateFilename("members", "xlsx");
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;

            case PDF:
                data = pdfExportService.exportMembersToPDF(members);
                filename = generateFilename("members", "pdf");
                mediaType = MediaType.APPLICATION_PDF;
                break;

            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    @GetMapping("/contributions/member/{memberId}/export")
    @Operation(summary = "Export member contributions report")
    public ResponseEntity<byte[]> exportMemberContributions(
            @PathVariable Long memberId,
            @RequestParam ExportFormat format) {
        log.info("GET /api/v1/reports/contributions/member/{}/export - Export as {}", memberId, format);

        List<ContributionResponse> contributions = contributionService.getMemberContributions(memberId);
        byte[] data;
        String filename;
        MediaType mediaType;

        switch (format) {
            case CSV:
                data = csvExportService.exportContributionsToCSV(contributions);
                filename = generateFilename("contributions_member_" + memberId, "csv");
                mediaType = MediaType.parseMediaType("text/csv");
                break;

            case EXCEL:
                data = excelExportService.exportContributionsToExcel(contributions);
                filename = generateFilename("contributions_member_" + memberId, "xlsx");
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;

            default:
                throw new IllegalArgumentException("Unsupported export format for contributions: " + format);
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    @GetMapping("/statistics/export")
    @Operation(summary = "Export system statistics report")
    public ResponseEntity<byte[]> exportSystemStatistics(@RequestParam ExportFormat format) {
        log.info("GET /api/v1/reports/statistics/export - Export as {}", format);

        SystemStatisticsDto statistics = analyticsService.getSystemStatistics();
        byte[] data;
        String filename;
        MediaType mediaType;

        switch (format) {
            case EXCEL:
                data = excelExportService.exportSystemStatisticsToExcel(statistics);
                filename = generateFilename("system_statistics", "xlsx");
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;

            case PDF:
                data = pdfExportService.exportSystemStatisticsToPDF(statistics);
                filename = generateFilename("system_statistics", "pdf");
                mediaType = MediaType.APPLICATION_PDF;
                break;

            default:
                throw new IllegalArgumentException("Unsupported export format for statistics: " + format);
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    private String generateFilename(String prefix, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s.%s", prefix, timestamp, extension);
    }
}
