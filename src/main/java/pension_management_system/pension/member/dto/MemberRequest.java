package pension_management_system.pension.member.dto;

import jakarta.validation.constraints.*;
import lombok.*;


import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MemberRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid Email Format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format. Must start with 1-9 and be 2-15 digits long")
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;


    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Employer ID (optional during registration)
    private Long employerId;

}
