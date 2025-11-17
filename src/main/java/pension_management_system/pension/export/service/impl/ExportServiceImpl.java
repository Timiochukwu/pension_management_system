package pension_management_system.pension.export.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.employer.entity.Employer;
import pension_management_system.pension.employer.repository.EmployerRepository;
import pension_management_system.pension.export.dto.ExportFormat;
import pension_management_system.pension.export.service.ExportService;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExportServiceImpl implements ExportService {

    private final MemberRepository memberRepository;
    private final EmployerRepository employerRepository;
    private final ContributionRepository contributionRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public ByteArrayOutputStream exportMembers(ExportFormat format) throws IOException {
        log.info("Exporting members in {} format", format);
        List<Member> members = memberRepository.findAll();

        return switch (format) {
            case CSV -> exportMembersToCSV(members);
            case EXCEL -> exportMembersToExcel(members);
            case PDF -> exportMembersToPDF(members);
        };
    }

    @Override
    public ByteArrayOutputStream exportEmployers(ExportFormat format) throws IOException {
        log.info("Exporting employers in {} format", format);
        List<Employer> employers = employerRepository.findAll();

        return switch (format) {
            case CSV -> exportEmployersToCSV(employers);
            case EXCEL -> exportEmployersToExcel(employers);
            case PDF -> exportEmployersToPDF(employers);
        };
    }

    @Override
    public ByteArrayOutputStream exportContributions(ExportFormat format) throws IOException {
        log.info("Exporting contributions in {} format", format);
        List<Contribution> contributions = contributionRepository.findAll();

        return switch (format) {
            case CSV -> exportContributionsToCSV(contributions);
            case EXCEL -> exportContributionsToExcel(contributions);
            case PDF -> exportContributionsToPDF(contributions);
        };
    }

    @Override
    public ByteArrayOutputStream exportContributionsByMember(Long memberId, ExportFormat format) throws IOException {
        log.info("Exporting contributions for member ID {} in {} format", memberId, format);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        List<Contribution> contributions = contributionRepository.findByMemberId(member);

        return switch (format) {
            case CSV -> exportContributionsToCSV(contributions);
            case EXCEL -> exportContributionsToExcel(contributions);
            case PDF -> exportContributionsToPDF(contributions);
        };
    }

    // CSV Export Methods
    private ByteArrayOutputStream exportMembersToCSV(List<Member> members) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream));

        // Write header
        String[] header = {"ID", "Member ID", "First Name", "Last Name", "Email", "Phone",
                          "Date of Birth", "Status", "Active", "Employer", "Created At"};
        writer.writeNext(header);

        // Write data
        for (Member member : members) {
            String[] data = {
                    String.valueOf(member.getId()),
                    member.getMemberId(),
                    member.getFirstName(),
                    member.getLastName(),
                    member.getEmail(),
                    member.getPhoneNumber(),
                    member.getDateOfBirth().format(DATE_FORMATTER),
                    member.getMemberStatus().toString(),
                    String.valueOf(member.getActive()),
                    member.getEmployer() != null ? member.getEmployer().getCompanyName() : "N/A",
                    member.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            };
            writer.writeNext(data);
        }

        writer.close();
        return outputStream;
    }

    private ByteArrayOutputStream exportEmployersToCSV(List<Employer> employers) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream));

        // Write header
        String[] header = {"ID", "Employer ID", "Company Name", "Registration Number",
                          "Email", "Phone", "Industry", "Active", "Member Count", "Created At"};
        writer.writeNext(header);

        // Write data
        for (Employer employer : employers) {
            String[] data = {
                    String.valueOf(employer.getId()),
                    employer.getEmployerId(),
                    employer.getCompanyName(),
                    employer.getRegistrationNumber(),
                    employer.getEmail(),
                    employer.getPhoneNumber(),
                    employer.getIndustry(),
                    String.valueOf(employer.getActive()),
                    String.valueOf(employer.getMembers().size()),
                    employer.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            };
            writer.writeNext(data);
        }

        writer.close();
        return outputStream;
    }

    private ByteArrayOutputStream exportContributionsToCSV(List<Contribution> contributions) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream));

        // Write header
        String[] header = {"ID", "Reference Number", "Member", "Amount", "Type",
                          "Payment Method", "Status", "Contribution Date", "Created At"};
        writer.writeNext(header);

        // Write data
        for (Contribution contribution : contributions) {
            String[] data = {
                    String.valueOf(contribution.getId()),
                    contribution.getReferenceNumber(),
                    contribution.getMember().getFirstName() + " " + contribution.getMember().getLastName(),
                    contribution.getContributionAmount().toString(),
                    contribution.getContributionType().toString(),
                    contribution.getPaymentMethod().toString(),
                    contribution.getStatus().toString(),
                    contribution.getContributionDate().format(DATE_FORMATTER),
                    contribution.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            };
            writer.writeNext(data);
        }

        writer.close();
        return outputStream;
    }

    // Excel Export Methods
    private ByteArrayOutputStream exportMembersToExcel(List<Member> members) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Members");

        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Member ID", "First Name", "Last Name", "Email", "Phone",
                           "Date of Birth", "Status", "Active", "Employer", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        for (Member member : members) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(member.getId());
            row.createCell(1).setCellValue(member.getMemberId());
            row.createCell(2).setCellValue(member.getFirstName());
            row.createCell(3).setCellValue(member.getLastName());
            row.createCell(4).setCellValue(member.getEmail());
            row.createCell(5).setCellValue(member.getPhoneNumber());
            row.createCell(6).setCellValue(member.getDateOfBirth().format(DATE_FORMATTER));
            row.createCell(7).setCellValue(member.getMemberStatus().toString());
            row.createCell(8).setCellValue(member.getActive());
            row.createCell(9).setCellValue(member.getEmployer() != null ? member.getEmployer().getCompanyName() : "N/A");
            row.createCell(10).setCellValue(member.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    private ByteArrayOutputStream exportEmployersToExcel(List<Employer> employers) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employers");

        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Employer ID", "Company Name", "Registration Number",
                           "Email", "Phone", "Industry", "Active", "Member Count", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Employer employer : employers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(employer.getId());
            row.createCell(1).setCellValue(employer.getEmployerId());
            row.createCell(2).setCellValue(employer.getCompanyName());
            row.createCell(3).setCellValue(employer.getRegistrationNumber());
            row.createCell(4).setCellValue(employer.getEmail());
            row.createCell(5).setCellValue(employer.getPhoneNumber());
            row.createCell(6).setCellValue(employer.getIndustry());
            row.createCell(7).setCellValue(employer.getActive());
            row.createCell(8).setCellValue(employer.getMembers().size());
            row.createCell(9).setCellValue(employer.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    private ByteArrayOutputStream exportContributionsToExcel(List<Contribution> contributions) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Contributions");

        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Reference Number", "Member", "Amount", "Type",
                           "Payment Method", "Status", "Contribution Date", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Contribution contribution : contributions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(contribution.getId());
            row.createCell(1).setCellValue(contribution.getReferenceNumber());
            row.createCell(2).setCellValue(contribution.getMember().getFirstName() + " " + contribution.getMember().getLastName());
            row.createCell(3).setCellValue(contribution.getContributionAmount().doubleValue());
            row.createCell(4).setCellValue(contribution.getContributionType().toString());
            row.createCell(5).setCellValue(contribution.getPaymentMethod().toString());
            row.createCell(6).setCellValue(contribution.getStatus().toString());
            row.createCell(7).setCellValue(contribution.getContributionDate().format(DATE_FORMATTER));
            row.createCell(8).setCellValue(contribution.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    // PDF Export Methods
    private ByteArrayOutputStream exportMembersToPDF(List<Member> members) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Members Report")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        float[] columnWidths = {50, 100, 100, 100, 150, 100, 100};
        Table table = new Table(columnWidths);
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        // Add headers
        String[] headers = {"ID", "Member ID", "Name", "Email", "Phone", "Status", "Active"};
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));
        }

        // Add data
        for (Member member : members) {
            table.addCell(String.valueOf(member.getId()));
            table.addCell(member.getMemberId());
            table.addCell(member.getFirstName() + " " + member.getLastName());
            table.addCell(member.getEmail());
            table.addCell(member.getPhoneNumber());
            table.addCell(member.getMemberStatus().toString());
            table.addCell(String.valueOf(member.getActive()));
        }

        document.add(table);
        document.close();

        return outputStream;
    }

    private ByteArrayOutputStream exportEmployersToPDF(List<Employer> employers) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Employers Report")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        float[] columnWidths = {50, 100, 150, 120, 150, 80};
        Table table = new Table(columnWidths);
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        String[] headers = {"ID", "Employer ID", "Company Name", "Reg Number", "Email", "Active"};
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));
        }

        for (Employer employer : employers) {
            table.addCell(String.valueOf(employer.getId()));
            table.addCell(employer.getEmployerId());
            table.addCell(employer.getCompanyName());
            table.addCell(employer.getRegistrationNumber());
            table.addCell(employer.getEmail());
            table.addCell(String.valueOf(employer.getActive()));
        }

        document.add(table);
        document.close();

        return outputStream;
    }

    private ByteArrayOutputStream exportContributionsToPDF(List<Contribution> contributions) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Contributions Report")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        float[] columnWidths = {50, 120, 120, 80, 100, 80};
        Table table = new Table(columnWidths);
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        String[] headers = {"ID", "Reference", "Member", "Amount", "Type", "Status"};
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));
        }

        for (Contribution contribution : contributions) {
            table.addCell(String.valueOf(contribution.getId()));
            table.addCell(contribution.getReferenceNumber());
            table.addCell(contribution.getMember().getFirstName() + " " + contribution.getMember().getLastName());
            table.addCell(contribution.getContributionAmount().toString());
            table.addCell(contribution.getContributionType().toString());
            table.addCell(contribution.getStatus().toString());
        }

        document.add(table);
        document.close();

        return outputStream;
    }

    // Helper method to create header style for Excel
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
