package pension_management_system.pension.export.service;

import pension_management_system.pension.export.dto.ExportFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service interface for data export functionality
 */
public interface ExportService {

    /**
     * Export members data
     */
    ByteArrayOutputStream exportMembers(ExportFormat format) throws IOException;

    /**
     * Export employers data
     */
    ByteArrayOutputStream exportEmployers(ExportFormat format) throws IOException;

    /**
     * Export contributions data
     */
    ByteArrayOutputStream exportContributions(ExportFormat format) throws IOException;

    /**
     * Export contributions for a specific member
     */
    ByteArrayOutputStream exportContributionsByMember(Long memberId, ExportFormat format) throws IOException;
}
