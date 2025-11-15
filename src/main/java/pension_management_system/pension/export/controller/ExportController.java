package pension_management_system.pension.export.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.export.dto.ExportFormat;
import pension_management_system.pension.export.service.ExportService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Export", description = "Data Export APIs (CSV, Excel, PDF)")
public class ExportController {

    private final ExportService exportService;

    @Operation(summary = "Export members", description = "Export all members in specified format (CSV, EXCEL, PDF)")
    @GetMapping("/members")
    public ResponseEntity<byte[]> exportMembers(
            @RequestParam(defaultValue = "CSV") ExportFormat format) {
        try {
            log.info("Exporting members in {} format", format);
            ByteArrayOutputStream outputStream = exportService.exportMembers(format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            headers.setContentDispositionFormData("attachment", "members." + getFileExtension(format));

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error exporting members", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Export employers", description = "Export all employers in specified format (CSV, EXCEL, PDF)")
    @GetMapping("/employers")
    public ResponseEntity<byte[]> exportEmployers(
            @RequestParam(defaultValue = "CSV") ExportFormat format) {
        try {
            log.info("Exporting employers in {} format", format);
            ByteArrayOutputStream outputStream = exportService.exportEmployers(format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            headers.setContentDispositionFormData("attachment", "employers." + getFileExtension(format));

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error exporting employers", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Export contributions", description = "Export all contributions in specified format (CSV, EXCEL, PDF)")
    @GetMapping("/contributions")
    public ResponseEntity<byte[]> exportContributions(
            @RequestParam(defaultValue = "CSV") ExportFormat format) {
        try {
            log.info("Exporting contributions in {} format", format);
            ByteArrayOutputStream outputStream = exportService.exportContributions(format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            headers.setContentDispositionFormData("attachment", "contributions." + getFileExtension(format));

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error exporting contributions", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Export member contributions", description = "Export contributions for a specific member")
    @GetMapping("/contributions/member/{memberId}")
    public ResponseEntity<byte[]> exportMemberContributions(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "CSV") ExportFormat format) {
        try {
            log.info("Exporting contributions for member {} in {} format", memberId, format);
            ByteArrayOutputStream outputStream = exportService.exportContributionsByMember(memberId, format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            headers.setContentDispositionFormData("attachment", "member_" + memberId + "_contributions." + getFileExtension(format));

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Member not found: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error("Error exporting member contributions", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private MediaType getMediaType(ExportFormat format) {
        return switch (format) {
            case CSV -> MediaType.parseMediaType("text/csv");
            case EXCEL -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case PDF -> MediaType.APPLICATION_PDF;
        };
    }

    private String getFileExtension(ExportFormat format) {
        return switch (format) {
            case CSV -> "csv";
            case EXCEL -> "xlsx";
            case PDF -> "pdf";
        };
    }
}
