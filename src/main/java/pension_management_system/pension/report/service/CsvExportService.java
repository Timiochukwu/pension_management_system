package pension_management_system.pension.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.member.dto.MemberResponse;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvExportService {

    public byte[] exportMembersToCSV(List<MemberResponse> members) {
        log.info("Exporting {} members to CSV", members.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(baos)) {

            // Header
            writer.println("Member ID,First Name,Last Name,Email,Phone,Date of Birth,Status,Active,Created At");

            // Data
            for (MemberResponse member : members) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        escapeCsv(member.getMemberId()),
                        escapeCsv(member.getFirstName()),
                        escapeCsv(member.getLastName()),
                        escapeCsv(member.getEmail()),
                        escapeCsv(member.getPhoneNumber()),
                        member.getDateOfBirth(),
                        member.getMemberStatus(),
                        member.getActive(),
                        member.getCreatedAt()
                );
            }

            writer.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting members to CSV", e);
            throw new RuntimeException("Failed to export members to CSV", e);
        }
    }

    public byte[] exportContributionsToCSV(List<ContributionResponse> contributions) {
        log.info("Exporting {} contributions to CSV", contributions.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(baos)) {

            // Header
            writer.println("Reference Number,Member ID,Type,Amount,Date,Payment Method,Status,Created At");

            // Data
            for (ContributionResponse contribution : contributions) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                        escapeCsv(contribution.getReferenceNumber()),
                        contribution.getMemberId(),
                        contribution.getContributionType(),
                        contribution.getContributionAmount(),
                        contribution.getContributionDate(),
                        contribution.getPaymentMethod(),
                        contribution.getStatus(),
                        contribution.getCreatedAt()
                );
            }

            writer.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting contributions to CSV", e);
            throw new RuntimeException("Failed to export contributions to CSV", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
