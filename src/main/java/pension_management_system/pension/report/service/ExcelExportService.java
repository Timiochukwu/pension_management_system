package pension_management_system.pension.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pension_management_system.pension.analytics.dto.SystemStatisticsDto;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.member.dto.MemberResponse;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportMembersToExcel(List<MemberResponse> members) {
        log.info("Exporting {} members to Excel", members.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Members");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Member ID", "First Name", "Last Name", "Email", "Phone", "Date of Birth", "Status", "Active", "Created At"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (MemberResponse member : members) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(member.getMemberId());
                row.createCell(1).setCellValue(member.getFirstName());
                row.createCell(2).setCellValue(member.getLastName());
                row.createCell(3).setCellValue(member.getEmail());
                row.createCell(4).setCellValue(member.getPhoneNumber());
                row.createCell(5).setCellValue(member.getDateOfBirth().format(DATE_FORMATTER));
                row.createCell(6).setCellValue(member.getMemberStatus() != null ? member.getMemberStatus().toString() : "");
                row.createCell(7).setCellValue(member.getActive() != null ? member.getActive().toString() : "");
                row.createCell(8).setCellValue(member.getCreatedAt() != null ? member.getCreatedAt().format(DATETIME_FORMATTER) : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting members to Excel", e);
            throw new RuntimeException("Failed to export members to Excel", e);
        }
    }

    public byte[] exportContributionsToExcel(List<ContributionResponse> contributions) {
        log.info("Exporting {} contributions to Excel", contributions.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Contributions");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Reference Number", "Member ID", "Type", "Amount", "Date", "Payment Method", "Status", "Created At"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (ContributionResponse contribution : contributions) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(contribution.getReferenceNumber());
                row.createCell(1).setCellValue(contribution.getMemberId() != null ? contribution.getMemberId().toString() : "");
                row.createCell(2).setCellValue(contribution.getContributionType() != null ? contribution.getContributionType().toString() : "");
                row.createCell(3).setCellValue(contribution.getContributionAmount() != null ? contribution.getContributionAmount().doubleValue() : 0);
                row.createCell(4).setCellValue(contribution.getContributionDate() != null ? contribution.getContributionDate().format(DATE_FORMATTER) : "");
                row.createCell(5).setCellValue(contribution.getPaymentMethod());
                row.createCell(6).setCellValue(contribution.getStatus() != null ? contribution.getStatus().toString() : "");
                row.createCell(7).setCellValue(contribution.getCreatedAt() != null ? contribution.getCreatedAt().format(DATETIME_FORMATTER) : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting contributions to Excel", e);
            throw new RuntimeException("Failed to export contributions to Excel", e);
        }
    }

    public byte[] exportSystemStatisticsToExcel(SystemStatisticsDto statistics) {
        log.info("Exporting system statistics to Excel");

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("System Statistics");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = workbook.createCellStyle();

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Pension Management System - Statistics Report");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            rowNum++; // Empty row

            // Members Section
            addStatRow(sheet, rowNum++, "MEMBERS", "", headerStyle);
            addStatRow(sheet, rowNum++, "Total Members", statistics.getTotalMembers().toString(), dataStyle);
            addStatRow(sheet, rowNum++, "Active Members", statistics.getActiveMembers().toString(), dataStyle);
            addStatRow(sheet, rowNum++, "Retired Members", statistics.getRetiredMembers().toString(), dataStyle);

            rowNum++; // Empty row

            // Contributions Section
            addStatRow(sheet, rowNum++, "CONTRIBUTIONS", "", headerStyle);
            addStatRow(sheet, rowNum++, "Total Contributions", statistics.getTotalContributions().toString(), dataStyle);
            addStatRow(sheet, rowNum++, "Total Amount", statistics.getTotalContributionAmount().toString(), dataStyle);

            rowNum++; // Empty row

            // Benefits Section
            addStatRow(sheet, rowNum++, "BENEFITS", "", headerStyle);
            addStatRow(sheet, rowNum++, "Total Benefits", statistics.getTotalBenefits().toString(), dataStyle);
            addStatRow(sheet, rowNum++, "Pending Benefits", statistics.getPendingBenefits().toString(), dataStyle);
            addStatRow(sheet, rowNum++, "Approved Benefits", statistics.getApprovedBenefits().toString(), dataStyle);
            addStatRow(sheet, rowNum++, "Disbursed Benefits", statistics.getDisbursedBenefits().toString(), dataStyle);
            addStatRow(sheet, rowNum++, "Total Benefits Amount", statistics.getTotalBenefitsAmount().toString(), dataStyle);

            rowNum++; // Empty row

            // Employers Section
            addStatRow(sheet, rowNum++, "EMPLOYERS", "", headerStyle);
            addStatRow(sheet, rowNum++, "Total Employers", statistics.getTotalEmployers().toString(), dataStyle);
            addStatRow(sheet, rowNum++, "Active Employers", statistics.getActiveEmployers().toString(), dataStyle);

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting system statistics to Excel", e);
            throw new RuntimeException("Failed to export system statistics to Excel", e);
        }
    }

    private void addStatRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);

        if (!value.isEmpty()) {
            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(value);
            valueCell.setCellStyle(style);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
