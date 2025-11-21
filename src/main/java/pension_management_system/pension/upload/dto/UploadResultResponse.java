package pension_management_system.pension.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for CSV upload results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultResponse {
    private int totalRecords;
    private int successfulImports;
    private int failedImports;

    @Builder.Default
    private List<RowError> errors = new ArrayList<>();

    public void addError(int rowNumber, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new RowError(rowNumber, message));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        private int rowNumber;
        private String message;
    }
}
