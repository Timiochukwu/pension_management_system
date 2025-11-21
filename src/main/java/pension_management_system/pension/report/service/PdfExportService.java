package pension_management_system.pension.report.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pension_management_system.pension.analytics.dto.SystemStatisticsDto;
import pension_management_system.pension.member.dto.MemberResponse;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF Export Service using iText 7 API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportMembersToPDF(List<MemberResponse> members) {
        log.info("Exporting {} members to PDF", members.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Title
            Paragraph title = new Paragraph("Members Report")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Generated date
            Paragraph date = new Paragraph("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(date);

            document.add(new Paragraph("\n"));

            // Table
            Table table = new Table(UnitValue.createPercentArray(8)).useAllAvailableWidth();

            // Header
            String[] headers = {"Member ID", "Name", "Email", "Phone", "DOB", "Status", "Active", "Created"};
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(headerCell);
            }

            // Data
            for (MemberResponse member : members) {
                addTableCell(table, member.getMemberId());
                addTableCell(table, member.getFullName());
                addTableCell(table, member.getEmail());
                addTableCell(table, member.getPhoneNumber());
                addTableCell(table, member.getDateOfBirth() != null ? member.getDateOfBirth().toString() : "");
                addTableCell(table, member.getMemberStatus() != null ? member.getMemberStatus().toString() : "");
                addTableCell(table, member.getActive() != null ? member.getActive().toString() : "");
                addTableCell(table, member.getCreatedAt() != null ? member.getCreatedAt().format(DATETIME_FORMATTER) : "");
            }

            document.add(table);

            // Footer
            document.add(new Paragraph("\nTotal Members: " + members.size()).setFontSize(10));

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
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Title
            Paragraph title = new Paragraph("Pension Management System\nStatistics Report")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Generated date
            Paragraph date = new Paragraph("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(date);

            document.add(new Paragraph("\n"));

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

    private void addSection(Document document, String sectionTitle) {
        Paragraph section = new Paragraph(sectionTitle)
                .setFontSize(14)
                .setBold();
        document.add(section);
    }

    private void addStatTable(Document document, String[][] data) {
        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

        for (String[] row : data) {
            Cell labelCell = new Cell()
                    .add(new Paragraph(row[0]).setFontSize(10))
                    .setBorder(null);
            table.addCell(labelCell);

            Cell valueCell = new Cell()
                    .add(new Paragraph(row[1]).setFontSize(10).setBold())
                    .setBorder(null);
            table.addCell(valueCell);
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTableCell(Table table, String value) {
        Cell cell = new Cell()
                .add(new Paragraph(value != null ? value : "").setFontSize(8));
        table.addCell(cell);
    }
}
