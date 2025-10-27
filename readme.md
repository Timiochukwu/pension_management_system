# Pension Management System - Quick Start Guide

## 🚀 Getting Started (Day 1 - Morning)

### Step 1: Update Your Project Structure

Copy all the artifacts I've created into your project:

```
src/main/java/pension_management_system/pension/
│
├── member/
│   ├── entity/
│   │   ├── Member.java ✅ (UPDATE with new version)
│   │   └── MemberStatus.java ✅ (NEW)
│   ├── service/
│   │   ├── MemberService.java ✅ (NEW)
│   │   └── MemberServiceImpl.java ✅ (NEW)
│   ├── controller/
│   │   └── MemberController.java ✅ (NEW)
│   └── dto/
│       └── MemberResponse.java ✅ (UPDATE)
│
├── contribution/
│   ├── entity/
│   │   └── Contribution.java ✅ (NEW - includes enums)
│   ├── repository/
│   │   └── ContributionRepository.java ✅ (NEW)
│   ├── service/
│   │   ├── ContributionService.java ✅ (NEW)
│   │   └── ContributionServiceImpl.java ✅ (NEW)
│   ├── controller/
│   │   └── ContributionController.java ✅ (NEW)
│   ├── dto/
│   │   ├── ContributionRequest.java ✅ (NEW)
│   │   ├── ContributionResponse.java ✅ (NEW)
│   │   └── ContributionStatementResponse.java ✅ (NEW)
│   └── mapper/
│       └── ContributionMapper.java ✅ (NEW)
│
├── employer/
│   └── entity/
│       └── Employer.java ✅ (NEW)
│
├── benefit/
│   └── entity/
│       └── Benefit.java ✅ (NEW - includes enums)
│
└── common/
    ├── dto/
    │   ├── ApiResponse.java ✅ (UPDATE)
    │   └── ErrorResponse.java ✅ (NEW)
    └── exception/
        ├── GlobalExceptionHandler.java ✅ (NEW)
        ├── MemberNotFoundException.java ✅ (NEW)
        ├── DuplicateMonthlyContributionException.java ✅ (NEW)
        └── InvalidContributionException.java ✅ (NEW)
```

### Step 2: Update MemberRepository

```java
package pension_management_system.pension.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    List<Member> findByStatus(MemberStatus status);
    List<Member> findByActiveTrue();

    @Query("SELECT m FROM Member m WHERE YEAR(CURRENT_DATE) - YEAR(m.dateOfBirth) >= 60")
    List<Member> findMembersEligibleForRetirement();
}
```

### Step 3: Compile Project

```bash
# Clean and compile to generate MapStruct implementations
./mvnw clean compile

# Check for any compilation errors
# MapStruct will generate MemberMapperImpl and ContributionMapperImpl
```

### Step 4: Run Application

```bash
# Start the application
./mvnw spring-boot:run

# Application should start on http://localhost:8080
```

---

## 📝 Testing Your APIs (Day 1 - Afternoon)

### Open Swagger UI
Visit: http://localhost:8080/swagger-ui.html

### Test Sequence (Use Postman or Swagger UI)

#### 1. Register a Member
```bash
POST http://localhost:8080/api/v1/members
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+2348012345678",
  "dateOfBirth": "1990-05-15",
  "address": "123 Main Street",
  "city": "Lagos",
  "state": "Lagos",
  "country": "Nigeria"
}
```

**Expected Response: 201 Created**
```json
{
  "success": true,
  "message": "Member registered successfully",
  "data": {
    "id": 1,
    "memberId": "MEM1699564800000",
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+2348012345678",
    "dateOfBirth": "1990-05-15",
    "age": 33,
    "status": "ACTIVE",
    "active": true,
    ...
  }
}
```

#### 2. Get Member by ID
```bash
GET http://localhost:8080/api/v1/members/1
```

#### 3. Create Monthly Contribution
```bash
POST http://localhost:8080/api/v1/contributions
Content-Type: application/json

{
  "memberId": 1,
  "contributionType": "MONTHLY",
  "amount": 50000.00,
  "contributionDate": "2023-11-15",
  "paymentMethod": "BANK_TRANSFER",
  "description": "Monthly contribution for November 2023"
}
```

**Expected Response: 201 Created**
```json
{
  "success": true,
  "message": "Contribution processed successfully",
  "data": {
    "id": 1,
    "referenceNumber": "CON20231115001234567890",
    "memberName": "John Doe",
    "contributionType": "MONTHLY",
    "amount": 50000.00,
    "status": "COMPLETED",
    ...
  }
}
```

#### 4. Try Duplicate Monthly Contribution (Should Fail)
```bash
POST http://localhost:8080/api/v1/contributions
Content-Type: application/json

{
  "memberId": 1,
  "contributionType": "MONTHLY",
  "amount": 50000.00,
  "contributionDate": "2023-11-20",
  "paymentMethod": "BANK_TRANSFER"
}
```

**Expected Response: 409 Conflict**
```json
{
  "success": false,
  "message": "Member MEM1699564800000 already has a monthly contribution for 2023-11...",
  "timestamp": "2023-11-15T10:30:00"
}
```

#### 5. Create Voluntary Contribution (Should Work)
```bash
POST http://localhost:8080/api/v1/contributions
Content-Type: application/json

{
  "memberId": 1,
  "contributionType": "VOLUNTARY",
  "amount": 100000.00,
  "contributionDate": "2023-11-20",
  "paymentMethod": "BANK_TRANSFER"
}
```

#### 6. Get All Member Contributions
```bash
GET http://localhost:8080/api/v1/contributions/member/1
```

#### 7. Calculate Total Contributions
```bash
GET http://localhost:8080/api/v1/contributions/member/1/total
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Total contributions calculated",
  "data": 150000.00
}
```

#### 8. Generate Contribution Statement
```bash
GET http://localhost:8080/api/v1/contributions/member/1/statement?startDate=2023-01-01&endDate=2023-12-31
```

---

## 🔨 What to Build Next (Day 2-3)

### Priority 1: Complete Employer Module

#### 1. Create EmployerRepository
```java
package pension_management_system.pension.employer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pension_management_system.pension.employer.entity.Employer;
import java.util.Optional;
import java.util.List;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
    Optional<Employer> findByEmployerId(String employerId);
    Optional<Employer> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
    List<Employer> findByActiveTrue();
}
```

#### 2. Create EmployerRequest DTO
```java
package pension_management_system.pension.employer.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerRequest {
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 200)
    private String companyName;

    @NotBlank(message = "Registration number is required")
    @Pattern(regexp = "^[A-Z0-9]{6,20}$")
    private String registrationNumber;

    @Email
    private String email;

    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String country;
    private String industry;
}
```

#### 3. Create EmployerResponse DTO
#### 4. Create EmployerMapper
#### 5. Create EmployerService & Implementation
#### 6. Create EmployerController

**Follow the same pattern as Member and Contribution modules!**

---

### Priority 2: Complete Benefit Module

Similar structure to Employer module:
- BenefitRepository
- BenefitRequest, BenefitResponse
- BenefitMapper
- BenefitService & Implementation
- BenefitController

Key business logic for BenefitService:
```java
/**
 * Calculate benefit eligibility
 * Rules:
 * 1. Member must have contributed for minimum period (e.g., 12 months)
 * 2. Total contributions must exceed minimum amount
 * 3. Member must be ACTIVE or RETIRED
 */
public BenefitResponse calculateBenefitEligibility(Long memberId) {
    // Get member
    // Get all contributions
    // Calculate contribution period (months)
    // Calculate total amount
    // Check eligibility rules
    // Create benefit record
    // Return response
}
```

---

### Priority 3: Background Jobs with Quartz (Day 3-4)

#### 1. Add Configuration (Already provided in checklist)

#### 2. Create Job: MonthlyContributionValidationJob
Location: `src/main/java/.../job/MonthlyContributionValidationJob.java`

**Purpose:** Check which members haven't made monthly contributions

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyContributionValidationJob implements Job {

    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Starting Monthly Contribution Validation Job");

        // Get current month
        YearMonth currentMonth = YearMonth.now();

        // Get all active members
        List<Member> activeMembers = memberRepository.findByActiveTrue();

        // Check each member
        for (Member member : activeMembers) {
            // Check if they have monthly contribution for current month
            Optional<Contribution> contribution = contributionRepository
                    .findMonthlyContributionByMemberAndYearMonth(
                            member,
                            ContributionType.MONTHLY,
                            currentMonth.getYear(),
                            currentMonth.getMonthValue()
                    );

            if (contribution.isEmpty()) {
                log.warn("Member {} missing monthly contribution for {}",
                        member.getMemberId(), currentMonth);
                // TODO: Send notification
            }
        }

        log.info("Monthly Contribution Validation Job completed");
    }
}
```

#### 3. Create More Jobs:
- BenefitEligibilityUpdateJob
- MonthlyInterestCalculationJob
- MemberStatementGenerationJob

---

## 🧪 Writing Tests (Day 4-5)

### Test Coverage Goals
- **Target: 70% minimum**
- Focus on service layer (business logic)
- Test positive and negative scenarios
- Test all business rules

### Test Structure (Already provided)
- Use the ContributionServiceTest example I created
- Follow the same pattern for other services

### Run Tests
```bash
# Run all tests
./mvnw test

# Generate coverage report
./mvnw test jacoco:report

# View report
open target/site/jacoco/index.html
```

---

## 📚 Documentation (Day 5-6)

### 1. README.md

```markdown
# Pension Management System

## Overview
A comprehensive pension contribution management system built with Spring Boot.

## Features
- Member registration and management
- Contribution processing (Monthly & Voluntary)
- Employer management
- Benefit calculation
- Automated background jobs
- Contribution statements

## Tech Stack
- Java 21
- Spring Boot 3.5.6
- MySQL Database
- MapStruct for object mapping
- Quartz for scheduling
- Swagger/OpenAPI for documentation

## Setup Instructions

### Prerequisites
- JDK 21
- MySQL 8.0+
- Maven 3.6+

### Database Setup
```sql
CREATE DATABASE java_pension_management_system;
```

### Configuration
Update `application.properties` with your database credentials.

### Run Application
```bash
./mvnw spring-boot:run
```

### Access API Documentation
http://localhost:8080/swagger-ui.html

## Architecture
- Clean Architecture principles
- Repository Pattern
- Service Layer for business logic
- DTO Pattern for API contracts

## API Endpoints

### Members
- POST /api/v1/members - Register new member
- GET /api/v1/members/{id} - Get member
- PUT /api/v1/members/{id} - Update member
- DELETE /api/v1/members/{id} - Delete member

### Contributions
- POST /api/v1/contributions - Process contribution
- GET /api/v1/contributions/member/{memberId} - Get contributions
- GET /api/v1/contributions/member/{memberId}/statement - Generate statement

## Business Rules
1. Only ONE monthly contribution per calendar month
2. Multiple voluntary contributions allowed
3. Members must be 18-70 years old
4. Minimum contribution period for benefits: 12 months

## Testing
```bash
./mvnw test
```

## Author
[Your Name]

## License
MIT
```

### 2. Export Postman Collection
- Use Swagger UI to generate collection
- Or manually create collection with all endpoints

---

## ✅ Final Checklist Before Submission

- [ ] All entities created
- [ ] All repositories implemented
- [ ] All services with business logic
- [ ] All controllers with proper error handling
- [ ] Background jobs configured and tested
- [ ] Unit tests written (70%+ coverage)
- [ ] Integration tests for critical paths
- [ ] README.md complete
- [ ] API documentation (Swagger)
- [ ] Database migrations tested
- [ ] Application runs without errors
- [ ] Postman collection exported
- [ ] Code pushed to GitHub (public repo)
- [ ] All comments and documentation added

---

## 🎯 Time Management

### Day 1: Core Setup (8 hours)
- Morning: Setup entities, repositories
- Afternoon: Member & Contribution services
- Evening: Controllers and testing

### Day 2: Employer & Benefit (8 hours)
- Morning: Employer module complete
- Afternoon: Benefit module complete
- Evening: Integration testing

### Day 3: Background Jobs (6 hours)
- Morning: Quartz configuration
- Afternoon: Implement all 4 jobs
- Evening: Test jobs

### Day 4-5: Testing (12 hours)
- Day 4: Write all unit tests
- Day 5: Integration tests and bug fixes

### Day 6: Documentation & Polish (6 hours)
- Morning: Complete README
- Afternoon: Final testing and cleanup
- Evening: Submit

---

## 🆘 Troubleshooting

### Issue: MapStruct not generating implementations
**Solution:**
```bash
./mvnw clean compile
```

### Issue: Database connection failed
**Solution:** Check `application.properties` credentials

### Issue: "Bean creation failed"
**Solution:** Ensure all classes have proper annotations:
- @Repository for repositories
- @Service for services
- @RestController for controllers

### Issue: Tests failing
**Solution:** Make sure you're using @ExtendWith(MockitoExtension.class)

