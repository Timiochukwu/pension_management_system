package pension_management_system.pension.upload.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.entity.PaymentMethod;
import pension_management_system.pension.employer.entity.Employer;
import pension_management_system.pension.employer.repository.EmployerRepository;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;
import pension_management_system.pension.member.repository.MemberRepository;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.upload.dto.UploadResultResponse;
import pension_management_system.pension.upload.service.UploadService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * UploadServiceImpl - Implementation of bulk CSV import functionality
 *
 * How CSV Upload Works:
 * 1. User uploads a CSV file through the API
 * 2. Service reads the file line by line using CSVReader
 * 3. Each row is validated before importing
 * 4. Valid rows are saved to database
 * 5. Invalid rows are collected as error messages
 * 6. Returns summary: total, successful, failed, and error details
 *
 * Why @Transactional?
 * - Each upload method runs in a single transaction
 * - If any critical error occurs, ALL imports are rolled back
 * - Prevents partial imports that could corrupt data
 *
 * Error Handling Strategy:
 * - Individual row failures don't stop the entire import
 * - Each failed row is logged with specific error message
 * - Successful rows are still imported
 * - User gets detailed report of what succeeded and what failed
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UploadServiceImpl implements UploadService {

    // DEPENDENCIES - Injected by Spring
    private final MemberRepository memberRepository;
    private final EmployerRepository employerRepository;
    private final ContributionRepository contributionRepository;

    // Date format used in CSV files: yyyy-MM-dd (e.g., 2025-01-15)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * UPLOAD MEMBERS FROM CSV
     *
     * Process:
     * 1. Validate file is CSV and not empty
     * 2. Read file using CSVReader
     * 3. Skip header row
     * 4. For each data row:
     *    a. Parse CSV values
     *    b. Validate data (email format, age, duplicates)
     *    c. Create Member entity
     *    d. Save to database or collect error
     * 5. Return summary of results
     *
     * @Transactional ensures all-or-nothing for critical errors
     * But individual row errors don't rollback successful imports
     */
    @Override
    @Transactional
    public UploadResultResponse uploadMembers(MultipartFile file) throws Exception {
        log.info("Starting member upload from CSV file: {}", file.getOriginalFilename());

        // STEP 1: Initialize result tracker
        UploadResultResponse result = UploadResultResponse.builder()
                .totalRecords(0)
                .successfulImports(0)
                .failedImports(0)
                .build();

        // STEP 2: Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be CSV format");
        }

        // STEP 3: Read CSV file
        // CSVReader automatically handles:
        // - Quoted fields (e.g., "John, Jr.")
        // - Escaped commas
        // - Line breaks within quoted fields
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            // STEP 4: Process each row (skip header at index 0)
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int rowNumber = i + 1; // +1 because CSV editors show row 1 as header
                result.setTotalRecords(result.getTotalRecords() + 1);

                try {
                    // STEP 4a: Validate row has enough columns
                    // Expected: firstName,lastName,email,phoneNumber,dateOfBirth,address,city,state,postalCode,country,employerId
                    if (row.length < 11) {
                        result.addError(rowNumber, "Missing required columns. Expected 11 columns, got " + row.length);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue; // Skip to next row
                    }

                    // STEP 4b: Parse values from CSV
                    String firstName = row[0].trim();
                    String lastName = row[1].trim();
                    String email = row[2].trim();
                    String phoneNumber = row[3].trim();
                    String dateOfBirthStr = row[4].trim();
                    String address = row[5].trim();
                    String city = row[6].trim();
                    String state = row[7].trim();
                    String postalCode = row[8].trim();
                    String country = row[9].trim();
                    String employerIdStr = row[10].trim();

                    // STEP 4c: Validate required fields
                    if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                        result.addError(rowNumber, "First name, last name, and email are required");
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    // STEP 4d: Check for duplicate email
                    if (memberRepository.existsByEmail(email)) {
                        result.addError(rowNumber, "Email already exists: " + email);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    // STEP 4e: Parse date of birth
                    LocalDate dateOfBirth;
                    try {
                        dateOfBirth = LocalDate.parse(dateOfBirthStr, DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        result.addError(rowNumber, "Invalid date format for dateOfBirth. Use yyyy-MM-dd");
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    // STEP 4f: Find employer if provided
                    Employer employer = null;
                    if (!employerIdStr.isEmpty()) {
                        try {
                            Long employerId = Long.parseLong(employerIdStr);
                            employer = employerRepository.findById(employerId).orElse(null);
                            if (employer == null) {
                                result.addError(rowNumber, "Employer not found with ID: " + employerId);
                                result.setFailedImports(result.getFailedImports() + 1);
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            result.addError(rowNumber, "Invalid employer ID format: " + employerIdStr);
                            result.setFailedImports(result.getFailedImports() + 1);
                            continue;
                        }
                    }

                    // STEP 4g: Create and save member
                    Member member = new Member();
                    member.setFirstName(firstName);
                    member.setLastName(lastName);
                    member.setEmail(email);
                    member.setPhoneNumber(phoneNumber);
                    member.setDateOfBirth(dateOfBirth);
                    member.setAddress(address);
                    member.setCity(city);
                    member.setState(state);
                    member.setPostalCode(postalCode);
                    member.setCountry(country);
                    member.setEmployer(employer);
                    member.setMemberStatus(MemberStatus.ACTIVE);
                    member.setActive(true);

                    memberRepository.save(member);
                    result.setSuccessfulImports(result.getSuccessfulImports() + 1);

                    log.debug("Successfully imported member: {}", email);

                } catch (Exception e) {
                    // Catch any unexpected errors for this row
                    result.addError(rowNumber, "Unexpected error: " + e.getMessage());
                    result.setFailedImports(result.getFailedImports() + 1);
                    log.error("Error importing member at row {}: {}", rowNumber, e.getMessage(), e);
                }
            }

            log.info("Member upload completed. Total: {}, Success: {}, Failed: {}",
                    result.getTotalRecords(), result.getSuccessfulImports(), result.getFailedImports());

            return result;

        } catch (IOException | CsvException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new Exception("Failed to read CSV file: " + e.getMessage());
        }
    }

    /**
     * UPLOAD EMPLOYERS FROM CSV
     *
     * Similar to uploadMembers but for employer data
     * Expected columns: companyName,registrationNumber,email,phoneNumber,address,city,state,postalCode,country,industry
     */
    @Override
    @Transactional
    public UploadResultResponse uploadEmployers(MultipartFile file) throws Exception {
        log.info("Starting employer upload from CSV file: {}", file.getOriginalFilename());

        UploadResultResponse result = UploadResultResponse.builder()
                .totalRecords(0)
                .successfulImports(0)
                .failedImports(0)
                .build();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be CSV format");
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int rowNumber = i + 1;
                result.setTotalRecords(result.getTotalRecords() + 1);

                try {
                    if (row.length < 10) {
                        result.addError(rowNumber, "Missing required columns. Expected 10 columns, got " + row.length);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    String companyName = row[0].trim();
                    String registrationNumber = row[1].trim();
                    String email = row[2].trim();
                    String phoneNumber = row[3].trim();
                    String address = row[4].trim();
                    String city = row[5].trim();
                    String state = row[6].trim();
                    String postalCode = row[7].trim();
                    String country = row[8].trim();
                    String industry = row[9].trim();

                    if (companyName.isEmpty() || registrationNumber.isEmpty()) {
                        result.addError(rowNumber, "Company name and registration number are required");
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    if (employerRepository.existsByRegistrationNumber(registrationNumber)) {
                        result.addError(rowNumber, "Registration number already exists: " + registrationNumber);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    if (employerRepository.existsByCompanyName(companyName)) {
                        result.addError(rowNumber, "Company name already exists: " + companyName);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    Employer employer = new Employer();
                    employer.setCompanyName(companyName);
                    employer.setRegistrationNumber(registrationNumber);
                    employer.setEmail(email);
                    employer.setPhoneNumber(phoneNumber);
                    employer.setAddress(address);
                    employer.setCity(city);
                    employer.setState(state);
                    employer.setPostalCode(postalCode);
                    employer.setCountry(country);
                    employer.setIndustry(industry);
                    employer.setActive(true);

                    employerRepository.save(employer);
                    result.setSuccessfulImports(result.getSuccessfulImports() + 1);

                    log.debug("Successfully imported employer: {}", companyName);

                } catch (Exception e) {
                    result.addError(rowNumber, "Unexpected error: " + e.getMessage());
                    result.setFailedImports(result.getFailedImports() + 1);
                    log.error("Error importing employer at row {}: {}", rowNumber, e.getMessage(), e);
                }
            }

            log.info("Employer upload completed. Total: {}, Success: {}, Failed: {}",
                    result.getTotalRecords(), result.getSuccessfulImports(), result.getFailedImports());

            return result;

        } catch (IOException | CsvException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new Exception("Failed to read CSV file: " + e.getMessage());
        }
    }

    /**
     * UPLOAD CONTRIBUTIONS FROM CSV
     *
     * Expected columns: memberId,contributionAmount,contributionType,paymentMethod,contributionDate,description
     */
    @Override
    @Transactional
    public UploadResultResponse uploadContributions(MultipartFile file) throws Exception {
        log.info("Starting contribution upload from CSV file: {}", file.getOriginalFilename());

        UploadResultResponse result = UploadResultResponse.builder()
                .totalRecords(0)
                .successfulImports(0)
                .failedImports(0)
                .build();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be CSV format");
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int rowNumber = i + 1;
                result.setTotalRecords(result.getTotalRecords() + 1);

                try {
                    if (row.length < 6) {
                        result.addError(rowNumber, "Missing required columns. Expected 6 columns, got " + row.length);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    String memberIdStr = row[0].trim();
                    String amountStr = row[1].trim();
                    String typeStr = row[2].trim();
                    String paymentMethodStr = row[3].trim();
                    String dateStr = row[4].trim();
                    String description = row[5].trim();

                    // Parse member ID
                    Long memberId;
                    try {
                        memberId = Long.parseLong(memberIdStr);
                    } catch (NumberFormatException e) {
                        result.addError(rowNumber, "Invalid member ID format: " + memberIdStr);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    Member member = memberRepository.findById(memberId).orElse(null);
                    if (member == null) {
                        result.addError(rowNumber, "Member not found with ID: " + memberId);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    // Parse amount
                    BigDecimal amount;
                    try {
                        amount = new BigDecimal(amountStr);
                    } catch (NumberFormatException e) {
                        result.addError(rowNumber, "Invalid amount format: " + amountStr);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    // Parse contribution type
                    ContributionType contributionType;
                    try {
                        contributionType = ContributionType.valueOf(typeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        result.addError(rowNumber, "Invalid contribution type: " + typeStr + ". Use MONTHLY or VOLUNTARY");
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    // Parse payment method
                    PaymentMethod paymentMethod;
                    try {
                        paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        result.addError(rowNumber, "Invalid payment method: " + paymentMethodStr);
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    // Parse date
                    LocalDateTime contributionDate;
                    try {
                        contributionDate = LocalDate.parse(dateStr, DATE_FORMATTER).atStartOfDay();
                    } catch (DateTimeParseException e) {
                        result.addError(rowNumber, "Invalid date format. Use yyyy-MM-dd");
                        result.setFailedImports(result.getFailedImports() + 1);
                        continue;
                    }

                    Contribution contribution = new Contribution();
                    contribution.setMember(member);
                    contribution.setContributionAmount(amount);
                    contribution.setContributionType(contributionType);
                    contribution.setPaymentMethod(paymentMethod);
                    contribution.setContributionDate(contributionDate);
                    contribution.setDescription(description);
                    contribution.setStatus(ContributionStatus.COMPLETED);

                    contributionRepository.save(contribution);
                    result.setSuccessfulImports(result.getSuccessfulImports() + 1);

                    log.debug("Successfully imported contribution for member: {}", memberId);

                } catch (Exception e) {
                    result.addError(rowNumber, "Unexpected error: " + e.getMessage());
                    result.setFailedImports(result.getFailedImports() + 1);
                    log.error("Error importing contribution at row {}: {}", rowNumber, e.getMessage(), e);
                }
            }

            log.info("Contribution upload completed. Total: {}, Success: {}, Failed: {}",
                    result.getTotalRecords(), result.getSuccessfulImports(), result.getFailedImports());

            return result;

        } catch (IOException | CsvException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new Exception("Failed to read CSV file: " + e.getMessage());
        }
    }
}
