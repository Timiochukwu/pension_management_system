package pension_management_system.pension.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BvnVerificationResponse {
    private Long id;
    private String bvnNumber;
    private String status;
    private Integer matchScore;
    private String verifiedFirstName;
    private String verifiedLastName;
    private String verifiedDateOfBirth;
    private String verifiedPhoneNumber;
    private LocalDateTime verificationDate;
    private String errorMessage;
}
