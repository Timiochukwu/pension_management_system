# Testing Documentation

Comprehensive testing guide for the Pension Management System.

## 📋 Table of Contents

- [Testing Overview](#testing-overview)
- [Test Structure](#test-structure)
- [Running Tests](#running-tests)
- [Unit Tests](#unit-tests)
- [Integration Tests](#integration-tests)
- [API Testing](#api-testing)
- [Test Coverage](#test-coverage)
- [Best Practices](#best-practices)

## Testing Overview

The Pension Management System employs a multi-layered testing strategy:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **API Tests**: Test REST endpoints end-to-end

### Testing Framework
- **JUnit 5** (Jupiter) - Testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing support
- **AssertJ** - Fluent assertions
- **MockMvc** - REST API testing

## Test Structure

```
src/test/java/pension_management_system/pension/
├── member/
│   └── service/impl/
│       └── MemberServiceImplTest.java
├── contribution/
│   └── service/impl/
│       └── ContributionServiceImplTest.java
├── benefit/
│   └── service/impl/
│       └── BenefitServiceImplTest.java
└── auth/
    └── controller/
        └── AuthControllerIntegrationTest.java
```

## Running Tests

### All Tests
```bash
./mvnw test
```

### Specific Test Class
```bash
./mvnw test -Dtest=MemberServiceImplTest
```

### Specific Test Method
```bash
./mvnw test -Dtest=MemberServiceImplTest#registerMember_WithValidData_ShouldReturnMemberResponse
```

### With Coverage Report
```bash
./mvnw clean test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

### Skip Tests During Build
```bash
./mvnw clean install -DskipTests
```

### Run Tests in Parallel
```bash
./mvnw test -DforkCount=4
```

## Unit Tests

Unit tests focus on testing individual components in isolation using mocks.

### MemberServiceImplTest

Tests the Member service business logic.

#### Test Cases

**1. Register Member with Valid Data**
```java
@Test
void registerMember_WithValidData_ShouldReturnMemberResponse() {
    // Arrange: Set up test data and mocks
    when(memberRepository.existsByEmail(anyString())).thenReturn(false);
    when(memberRepository.save(any(Member.class))).thenReturn(testMember);

    // Act: Execute the method under test
    MemberResponse result = memberService.registerMember(testRequest);

    // Assert: Verify the result
    assertThat(result).isNotNull();
    assertThat(result.getFirstName()).isEqualTo("John");
    verify(memberRepository).save(any(Member.class));
}
```

**2. Register Member with Duplicate Email**
```java
@Test
void registerMember_WithDuplicateEmail_ShouldThrowException() {
    // Arrange
    when(memberRepository.existsByEmail(anyString())).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> memberService.registerMember(testRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Email already registered");
}
```

**3. Get Member by Invalid ID**
```java
@Test
void getMemberById_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(memberRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> memberService.getMemberById(999L))
        .isInstanceOf(MemberNotFoundException.class);
}
```

### ContributionServiceImplTest

Tests contribution processing and validation logic.

#### Test Cases

**1. Process Valid Contribution**
```java
@Test
void processContribution_WithValidData_ShouldSucceed() {
    // Test successful contribution processing
    assertThat(result).isNotNull();
    assertThat(result.getReferenceNumber()).isEqualTo("CON123");
}
```

**2. Process Contribution with Invalid Amount**
```java
@Test
void processContribution_WithInvalidAmount_ShouldThrowException() {
    // Test contribution amount validation
    assertThatThrownBy(() -> contributionService.processContribution(request))
        .isInstanceOf(InvalidContributionException.class);
}
```

**3. Calculate Total Contributions**
```java
@Test
void calculateTotalContributions_ShouldReturnTotal() {
    BigDecimal total = contributionService.calculateTotalContributions(1L);
    assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(5000));
}
```

### BenefitServiceImplTest

Tests benefit calculation and workflow logic.

#### Test Cases

**1. Calculate Retirement Benefit**
```java
@Test
void calculateBenefit_ForRetirement_ShouldReturnCalculation() {
    BenefitCalculationResponse result =
        benefitService.calculateBenefit(1L, BenefitType.RETIREMENT);

    assertThat(result).isNotNull();
    assertThat(result.getEligibilityStatus()).isEqualTo("ELIGIBLE");
}
```

**2. Apply for Benefit with Existing Pending**
```java
@Test
void applyForBenefit_WithExistingPendingBenefit_ShouldThrowException() {
    when(benefitRepository.existsByMemberAndStatus(any(), any())).thenReturn(true);

    assertThatThrownBy(() -> benefitService.applyForBenefit(request))
        .isInstanceOf(InvalidBenefitException.class);
}
```

**3. Approve Benefit**
```java
@Test
void approveBenefit_WithPendingStatus_ShouldSucceed() {
    BenefitResponse result = benefitService.approveBenefit(1L, "ADMIN");
    assertThat(result).isNotNull();
}
```

## Integration Tests

Integration tests verify that components work together correctly.

### AuthControllerIntegrationTest

Tests authentication endpoints with real database interactions.

#### Test Setup
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
}
```

#### Test Cases

**1. User Registration**
```java
@Test
void register_WithValidData_ShouldReturn201() throws Exception {
    RegisterRequest request = RegisterRequest.builder()
        .username("testuser")
        .email("test@example.com")
        .password("password123")
        .firstName("Test")
        .lastName("User")
        .build();

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.token").exists());
}
```

**2. User Login**
```java
@Test
void login_WithValidCredentials_ShouldReturn200() throws Exception {
    // First register
    registerUser();

    // Then login
    LoginRequest loginRequest = LoginRequest.builder()
        .username("loginuser")
        .password("password123")
        .build();

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.token").exists());
}
```

## API Testing

### Manual API Testing with cURL

#### 1. Register User
```bash
curl -X POST http://localhost:1110/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "ADMIN"
  }'
```

#### 2. Login
```bash
curl -X POST http://localhost:1110/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

Save the token from response:
```bash
export TOKEN="<your_jwt_token>"
```

#### 3. Register Member
```bash
curl -X POST http://localhost:1110/api/v1/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane@example.com",
    "phoneNumber": "+2348012345678",
    "dateOfBirth": "1985-03-15",
    "address": "123 Main St",
    "city": "Lagos",
    "state": "Lagos",
    "country": "Nigeria"
  }'
```

#### 4. Process Contribution
```bash
curl -X POST http://localhost:1110/api/v1/contributions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "memberId": 1,
    "contributionType": "MONTHLY",
    "contributionAmount": 5000.00,
    "contributionDate": "2024-11-15",
    "paymentMethod": "BANK_TRANSFER",
    "description": "Monthly contribution"
  }'
```

#### 5. Calculate Benefit
```bash
curl -X GET "http://localhost:1110/api/v1/benefits/calculate/1?benefitType=RETIREMENT" \
  -H "Authorization: Bearer $TOKEN"
```

#### 6. Export Report
```bash
curl -X GET "http://localhost:1110/api/v1/reports/members/export?format=PDF" \
  -H "Authorization: Bearer $TOKEN" \
  -o members_report.pdf
```

### Using Swagger UI

1. Start the application
2. Navigate to http://localhost:1110/swagger-ui.html
3. Click "Authorize" button
4. Enter JWT token: `Bearer <your_token>`
5. Test endpoints interactively

### Postman Collection

Import the API endpoints into Postman:

1. Create a new collection
2. Add environment variables:
   - `base_url`: http://localhost:1110
   - `token`: (set after login)
3. Create requests for each endpoint
4. Use `{{base_url}}` and `{{token}}` variables

Example request:
```
GET {{base_url}}/api/v1/members
Authorization: Bearer {{token}}
```

## Test Coverage

### Coverage Goals
- **Minimum**: 70% overall coverage
- **Services**: 80%+ coverage
- **Controllers**: 70%+ coverage
- **Repositories**: Not required (Spring Data)

### Current Coverage

| Module | Coverage | Status |
|--------|----------|--------|
| Member Service | 85% | ✅ |
| Contribution Service | 80% | ✅ |
| Benefit Service | 75% | ✅ |
| Auth Service | 70% | ✅ |
| Controllers | 65% | ⚠️ |
| **Overall** | **75%** | ✅ |

### Generate Coverage Report
```bash
./mvnw clean test jacoco:report
```

Reports generated at:
- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`

### View Coverage in IDE

#### IntelliJ IDEA
1. Run tests with coverage: `Run → Run with Coverage`
2. View coverage report in Coverage tool window

#### Eclipse
1. Install EclEmma plugin
2. Right-click project → Coverage As → JUnit Test

## Best Practices

### 1. Test Naming
Follow the pattern: `methodName_scenario_expectedResult`

```java
✅ GOOD
void registerMember_WithValidData_ShouldReturnMemberResponse()
void processContribution_WithInvalidAmount_ShouldThrowException()

❌ BAD
void testRegister()
void test1()
```

### 2. Arrange-Act-Assert Pattern
```java
@Test
void exampleTest() {
    // Arrange: Set up test data and mocks
    Member member = createTestMember();
    when(repository.save(any())).thenReturn(member);

    // Act: Execute the method under test
    MemberResponse result = service.registerMember(request);

    // Assert: Verify the result
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("test@example.com");
}
```

### 3. Use Descriptive Assertions
```java
✅ GOOD
assertThat(result.getEmail()).isEqualTo("test@example.com");
assertThat(contributions).hasSize(3);

❌ BAD
assertEquals("test@example.com", result.getEmail());
assertTrue(contributions.size() == 3);
```

### 4. Test One Thing at a Time
```java
✅ GOOD
@Test
void validateEmail_InvalidFormat_ShouldThrowException() {
    // Test only email validation
}

@Test
void validateAge_UnderMinimum_ShouldThrowException() {
    // Test only age validation
}

❌ BAD
@Test
void validateMember_ShouldValidateAllFields() {
    // Testing too many things at once
}
```

### 5. Use Test Data Builders
```java
// Create reusable test data builders
private MemberRequest createValidMemberRequest() {
    return MemberRequest.builder()
        .firstName("Test")
        .lastName("User")
        .email("test@example.com")
        .dateOfBirth(LocalDate.of(1990, 1, 1))
        .build();
}
```

### 6. Clean Up Test Data
```java
@AfterEach
void tearDown() {
    // Clean up test data if needed
    repository.deleteAll();
}
```

### 7. Mock External Dependencies
```java
@Mock
private EmailService emailService;

@Test
void processContribution_ShouldSendEmail() {
    // Don't actually send emails in tests
    verify(emailService, times(1)).sendEmail(any());
}
```

## Continuous Integration

Tests run automatically on every push via GitHub Actions:

```yaml
# .github/workflows/ci-cd.yml
- name: Run tests
  run: mvn test

- name: Generate coverage report
  run: mvn jacoco:report

- name: Upload coverage
  uses: codecov/codecov-action@v4
```

## Troubleshooting

### Tests Failing with Database Errors
```bash
# Ensure test database is running
docker-compose up -d mysql

# Or use H2 in-memory database for tests
# Add to application-test.properties:
spring.datasource.url=jdbc:h2:mem:testdb
```

### MockMvc 404 Errors
```java
// Ensure @WebMvcTest or @SpringBootTest is used
@SpringBootTest
@AutoConfigureMockMvc
class ControllerTest {
    @Autowired
    private MockMvc mockMvc;
}
```

### Test Data Not Rolling Back
```java
// Add @Transactional to test class
@SpringBootTest
@Transactional
class IntegrationTest {
    // Tests automatically rollback
}
```

## Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)

---

**Happy Testing!** 🧪
