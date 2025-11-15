package pension_management_system.pension.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * UploadResultResponse DTO
 *
 * Purpose: Contains the result of a CSV file upload operation
 * Shows how many records were successfully imported and which ones failed
 *
 * Used by: Upload endpoints to return import summary to the user
 *
 * Example Response:
 * {
 *   "totalRecords": 100,
 *   "successfulImports": 95,
 *   "failedImports": 5,
 *   "errors": [
 *     "Row 3: Email already exists - john@example.com",
 *     "Row 7: Invalid date format for dateOfBirth",
 *     ...
 *   ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultResponse {

    /**
     * Total number of records found in the CSV file
     * Includes both successful and failed imports
     */
    private int totalRecords;

    /**
     * Number of records successfully imported to database
     * These records passed all validation checks
     */
    private int successfulImports;

    /**
     * Number of records that failed to import
     * These had validation errors or duplicate data
     */
    private int failedImports;

    /**
     * List of error messages for failed imports
     * Each message includes the row number and reason for failure
     *
     * Example: "Row 5: Phone number already exists - +2348012345678"
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * Helper method to add an error message
     *
     * @param rowNumber The row number in CSV (starting from 1, excluding header)
     * @param message The error message describing what went wrong
     */
    public void addError(int rowNumber, String message) {
        errors.add("Row " + rowNumber + ": " + message);
    }
}
