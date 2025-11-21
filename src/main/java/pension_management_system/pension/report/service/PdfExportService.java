package pension_management_system.pension.report.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pension_management_system.pension.analytics.dto.SystemStatisticsDto;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.member.dto.MemberResponse;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportMembersToPDF(List<MemberResponse> members) {
        log.info("Exporting {} members to PDF", members.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);

            document.open();

            // Title
            Paragraph title = new Paragraph("Members Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Generated date
            Paragraph date = new Paragraph("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER), NORMAL_FONT);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // Table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);

            // Header
            addTableHeader(table, new String[]{
                    "Member ID", "Name", "Email", "Phone", "DOB", "Status", "Active", "Created"
            });

            // Data
            for (MemberResponse member : members) {
                addTableCell(table, member.getMemberId());
                addTableCell(table, member.getFullName());
                addTableCell(table, member.getEmail());
                addTableCell(table, member.getPhoneNumber());
                addTableCell(table, member.getDateOfBirth().toString());
                addTableCell(table, member.getMemberStatus() != null ? member.getMemberStatus().toString() : "");
                addTableCell(table, member.getActive() != null ? member.getActive().toString() : "");
                addTableCell(table, member.getCreatedAt() != null ? member.getCreatedAt().format(DATETIME_FORMATTER) : "");
            }

            document.add(table);

            // Footer
            Paragraph footer = new Paragraph("Total Members: " + members.size(), NORMAL_FONT);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting members to PDF", e);
            throw new RuntimeException("Failed to export members to PDF", e);
        }
    }

    public byte[] exportSystemStatisticsToPDF(SystemStatisticsDto statistics) {
        log.info("Exporting system statistics to PDF");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Title
            Paragraph title = new Paragraph("Pension Management System\nStatistics Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Generated date
            Paragraph date = new Paragraph("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER), NORMAL_FONT);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(30);
            document.add(date);

            // Members Section
            addSection(document, "MEMBERS OVERVIEW");
            addStatTable(document, new String[][]{
                    {"Total Members", statistics.getTotalMembers().toString()},
                    {"Active Members", statistics.getActiveMembers().toString()},
                    {"Retired Members", statistics.getRetiredMembers().toString()}
            });

            // Contributions Section
            addSection(document, "CONTRIBUTIONS OVERVIEW");
            addStatTable(document, new String[][]{
                    {"Total Contributions", statistics.getTotalContributions().toString()},
                    {"Total Contribution Amount", "₦" + statistics.getTotalContributionAmount().toString()}
            });

            // Benefits Section
            addSection(document, "BENEFITS OVERVIEW");
            addStatTable(document, new String[][]{
                    {"Total Benefits", statistics.getTotalBenefits().toString()},
                    {"Pending Benefits", statistics.getPendingBenefits().toString()},
                    {"Approved Benefits", statistics.getApprovedBenefits().toString()},
                    {"Disbursed Benefits", statistics.getDisbursedBenefits().toString()},
                    {"Total Benefits Amount", "₦" + statistics.getTotalBenefitsAmount().toString()}
            });

            // Employers Section
            addSection(document, "EMPLOYERS OVERVIEW");
            addStatTable(document, new String[][]{
                    {"Total Employers", statistics.getTotalEmployers().toString()},
                    {"Active Employers", statistics.getActiveEmployers().toString()}
            });

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting system statistics to PDF", e);
            throw new RuntimeException("Failed to export system statistics to PDF", e);
        }
    }

    private void addSection(Document document, String sectionTitle) throws DocumentException {
        Paragraph section = new Paragraph(sectionTitle, new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        section.setSpacingBefore(20);
        section.setSpacingAfter(10);
        document.add(section);
    }

    private void addStatTable(Document document, String[][] data) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setSpacingAfter(10);

        for (String[] row : data) {
            PdfPCell labelCell = new PdfPCell(new Phrase(row[0], NORMAL_FONT));
            labelCell.setBorder(Rectangle.NO_BORDER);
            labelCell.setPadding(8);
            table.addCell(labelCell);

            PdfPCell valueCell = new PdfPCell(new Phrase(row[1], new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            valueCell.setBorder(Rectangle.NO_BORDER);
            valueCell.setPadding(8);
            table.addCell(valueCell);
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", NORMAL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
