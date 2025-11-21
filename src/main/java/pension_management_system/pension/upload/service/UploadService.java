package pension_management_system.pension.upload.service;

import org.springframework.web.multipart.MultipartFile;
import pension_management_system.pension.upload.dto.UploadResultResponse;

/**
 * Service interface for CSV upload/import functionality
 */
public interface UploadService {

    /**
     * Upload members from CSV file
     */
    UploadResultResponse uploadMembers(MultipartFile file) throws Exception;

    /**
     * Upload employers from CSV file
     */
    UploadResultResponse uploadEmployers(MultipartFile file) throws Exception;

    /**
     * Upload contributions from CSV file
     */
    UploadResultResponse uploadContributions(MultipartFile file) throws Exception;
}
