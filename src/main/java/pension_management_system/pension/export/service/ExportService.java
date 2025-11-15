package pension_management_system.pension.export.service;

import pension_management_system.pension.export.dto.ExportFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public interface ExportService {
    ByteArrayOutputStream exportMembers(ExportFormat format) throws IOException;
    ByteArrayOutputStream exportEmployers(ExportFormat format) throws IOException;
    ByteArrayOutputStream exportContributions(ExportFormat format) throws IOException;
    ByteArrayOutputStream exportContributionsByMember(Long memberId, ExportFormat format) throws IOException;
}
