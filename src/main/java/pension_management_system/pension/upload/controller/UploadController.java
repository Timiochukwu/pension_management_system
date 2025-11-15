package pension_management_system.pension.upload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pension_management_system.pension.common.dto.ApiResponseDto;
import pension_management_system.pension.upload.dto.UploadResultResponse;
import pension_management_system.pension.upload.service.UploadService;

/**
 * UploadController - REST API endpoints for bulk CSV imports
 *
 * Purpose: Allow administrators to upload CSV files and import multiple records at once
 * This is much faster than creating records one by one through the UI
 *
 * How File Upload Works in Spring:
 * 1. Client sends HTTP POST request with Content-Type: multipart/form-data
 * 2. Spring receives file as MultipartFile object
 * 3. We validate file (size, type, content)
 * 4. We parse CSV and import data
 * 5. We return summary of successful and failed imports
 *
 * Annotations Explained:
 * @RestController - Marks this as a REST API controller
 * @RequestMapping - Base URL for all endpoints (/api/v1/upload)
 * @RequiredArgsConstructor - Lombok creates constructor for final fields (dependency injection)
 * @Slf4j - Lombok provides logging (log.info(), log.error(), etc.)
 * @Tag - Swagger/OpenAPI documentation grouping
 */
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Upload", description = "Bulk import APIs via CSV files")
public class UploadController {

    // DEPENDENCY INJECTION
    // Spring automatically injects UploadService implementation
    private final UploadService uploadService;

    /**
     * UPLOAD MEMBERS FROM CSV FILE
     *
     * HTTP Method: POST
     * URL: /api/v1/upload/members
     * Content-Type: multipart/form-data
     *
     * How to test with cURL:
     * curl -X POST http://localhost:1110/api/v1/upload/members \
     *   -F "file=@members.csv"
     *
     * How to test with Postman:
     * 1. Select POST method
     * 2. Enter URL: http://localhost:1110/api/v1/upload/members
     * 3. Go to Body tab
     * 4. Select form-data
     * 5. Add key "file" with type "File"
     * 6. Choose your CSV file
     * 7. Click Send
     *
     * CSV Format Example:
     * firstName,lastName,email,phoneNumber,dateOfBirth,address,city,state,postalCode,country,employerId
     * John,Doe,john@example.com,+2348012345678,1990-05-15,123 Main St,Lagos,Lagos,100001,Nigeria,1
     * Jane,Smith,jane@example.com,+2348012345679,1985-03-20,456 Oak Ave,Abuja,FCT,900001,Nigeria,2
     *
     * Response Example:
     * {
     *   "success": true,
     *   "message": "Upload completed. 2 successful, 0 failed",
     *   "data": {
     *     "totalRecords": 2,
     *     "successfulImports": 2,
     *     "failedImports": 0,
     *     "errors": []
     *   }
     * }
     *
     * @param file The CSV file uploaded by user
     *             Spring automatically maps "file" form parameter to MultipartFile
     * @return ResponseEntity with upload results
     */
    @PostMapping(value = "/members", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Bulk import members from CSV",
            description = "Upload a CSV file to import multiple members at once. " +
                    "Returns summary of successful and failed imports with error details."
    )
    public ResponseEntity<ApiResponseDto<UploadResultResponse>> uploadMembers(
            @RequestParam("file") MultipartFile file) {

        // Log the upload request for monitoring
        log.info("Received member upload request. File: {}, Size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            // STEP 1: Validate file is provided
            if (file.isEmpty()) {
                ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                        .success(false)
                        .message("File is empty. Please upload a valid CSV file.")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // STEP 2: Call service to process upload
            // Service handles:
            // - Reading CSV file
            // - Validating each row
            // - Importing valid records
            // - Collecting errors for invalid records
            UploadResultResponse result = uploadService.uploadMembers(file);

            // STEP 3: Build success response with results
            String message = String.format(
                    "Upload completed. %d successful, %d failed out of %d total records",
                    result.getSuccessfulImports(),
                    result.getFailedImports(),
                    result.getTotalRecords()
            );

            ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                    .success(true)
                    .message(message)
                    .data(result)
                    .build();

            // Return HTTP 200 OK with results
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Handle validation errors (wrong file type, empty file, etc.)
            log.error("Validation error during member upload: {}", e.getMessage());

            ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                    .success(false)
                    .message("Validation error: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            // Handle unexpected errors
            log.error("Error during member upload: {}", e.getMessage(), e);

            ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                    .success(false)
                    .message("Upload failed: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * UPLOAD EMPLOYERS FROM CSV FILE
     *
     * Similar to member upload but for employer data
     *
     * URL: POST /api/v1/upload/employers
     *
     * CSV Format:
     * companyName,registrationNumber,email,phoneNumber,address,city,state,postalCode,country,industry
     * Tech Corp,RC123456,info@techcorp.com,+2348012345678,456 Corp Ave,Lagos,Lagos,100001,Nigeria,Technology
     */
    @PostMapping(value = "/employers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Bulk import employers from CSV",
            description = "Upload a CSV file to import multiple employers at once"
    )
    public ResponseEntity<ApiResponseDto<UploadResultResponse>> uploadEmployers(
            @RequestParam("file") MultipartFile file) {

        log.info("Received employer upload request. File: {}, Size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            if (file.isEmpty()) {
                ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                        .success(false)
                        .message("File is empty. Please upload a valid CSV file.")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            UploadResultResponse result = uploadService.uploadEmployers(file);

            String message = String.format(
                    "Upload completed. %d successful, %d failed out of %d total records",
                    result.getSuccessfulImports(),
                    result.getFailedImports(),
                    result.getTotalRecords()
            );

            ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                    .success(true)
                    .message(message)
                    .data(result)
                    .build();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error during employer upload: {}", e.getMessage());

            ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                    .success(false)
                    .message("Validation error: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Error during employer upload: {}", e.getMessage(), e);

            ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                    .success(false)
                    .message("Upload failed: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * UPLOAD CONTRIBUTIONS FROM CSV FILE
     *
     * URL: POST /api/v1/upload/contributions
     *
     * CSV Format:
     * memberId,contributionAmount,contributionType,paymentMethod,contributionDate,description
     * 1,5000.00,MONTHLY,BANK_TRANSFER,2025-01-15,January contribution
     * 2,3000.00,VOLUNTARY,CASH,2025-01-20,Extra contribution
     */
    @PostMapping(value = "/contributions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Bulk import contributions from CSV",
            description = "Upload a CSV file to import multiple contributions at once"
    )
    public ResponseEntity<ApiResponseDto<UploadResultResponse>> uploadContributions(
            @RequestParam("file") MultipartFile file) {

        log.info("Received contribution upload request. File: {}, Size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            if (file.isEmpty()) {
                ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                        .success(false)
                        .message("File is empty. Please upload a valid CSV file.")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            UploadResultResponse result = uploadService.uploadContributions(file);

            String message = String.format(
                    "Upload completed. %d successful, %d failed out of %d total records",
                    result.getSuccessfulImports(),
                    result.getFailedImports(),
                    result.getTotalRecords()
            );

            ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                    .success(true)
                    .message(message)
                    .data(result)
                    .build();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error during contribution upload: {}", e.getMessage());

            ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                    .success(false)
                    .message("Validation error: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Error during contribution upload: {}", e.getMessage(), e);

            ApiResponseDto<UploadResultResponse> response = ApiResponseDto.<UploadResultResponse>builder()
                    .success(false)
                    .message("Upload failed: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
