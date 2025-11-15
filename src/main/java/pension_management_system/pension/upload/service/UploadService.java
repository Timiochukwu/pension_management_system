package pension_management_system.pension.upload.service;

import org.springframework.web.multipart.MultipartFile;
import pension_management_system.pension.upload.dto.UploadResultResponse;

/**
 * UploadService Interface
 *
 * Purpose: Defines contract for bulk import operations via CSV files
 * Allows administrators to upload CSV files and import multiple records at once
 *
 * Why use bulk upload?
 * - Faster than creating records one by one through UI
 * - Useful for initial system setup or data migration
 * - Can import hundreds/thousands of records in one operation
 *
 * What is MultipartFile?
 * - Spring's representation of an uploaded file
 * - Contains the file content, name, size, etc.
 * - Received from HTML form with enctype="multipart/form-data"
 */
public interface UploadService {

    /**
     * Upload and import members from CSV file
     *
     * CSV Format Expected:
     * firstName,lastName,email,phoneNumber,dateOfBirth,address,city,state,postalCode,country,employerId
     * John,Doe,john@example.com,+2348012345678,1990-05-15,123 Main St,Lagos,Lagos,100001,Nigeria,1
     *
     * @param file The CSV file uploaded by user
     * @return UploadResultResponse with import statistics and errors
     * @throws Exception if file is not CSV or cannot be read
     */
    UploadResultResponse uploadMembers(MultipartFile file) throws Exception;

    /**
     * Upload and import employers from CSV file
     *
     * CSV Format Expected:
     * companyName,registrationNumber,email,phoneNumber,address,city,state,postalCode,country,industry
     * Tech Corp,RC123456,info@techcorp.com,+2348012345678,456 Corp Ave,Lagos,Lagos,100001,Nigeria,Technology
     *
     * @param file The CSV file uploaded by user
     * @return UploadResultResponse with import statistics and errors
     * @throws Exception if file is not CSV or cannot be read
     */
    UploadResultResponse uploadEmployers(MultipartFile file) throws Exception;

    /**
     * Upload and import contributions from CSV file
     *
     * CSV Format Expected:
     * memberId,contributionAmount,contributionType,paymentMethod,contributionDate,description
     * 1,5000.00,MONTHLY,BANK_TRANSFER,2025-01-15,January contribution
     *
     * @param file The CSV file uploaded by user
     * @return UploadResultResponse with import statistics and errors
     * @throws Exception if file is not CSV or cannot be read
     */
    UploadResultResponse uploadContributions(MultipartFile file) throws Exception;
}
