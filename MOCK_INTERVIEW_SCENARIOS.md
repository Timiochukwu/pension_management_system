# Mock Interview Scenarios
## Practice Sessions for Backend Developer Role

---

## 🎭 Scenario 1: Technical Deep Dive (30 minutes)

### **Interviewer Opening:**
"Good morning! Thanks for joining us today. I'm the lead backend developer here at Zeedlabs. I've reviewed your resume and I see you've worked on a pension management system. Let's dive into that. Can you give me a quick overview of the project?"

### **Your Response (Practice This!):**
"Absolutely! I built a Pension Management System using Spring Boot 3.2.5 and Java 22. It's an enterprise application that manages the complete lifecycle of pension fund operations - from member enrollment to retirement benefit disbursements.

The system handles:
- Member registration and profile management
- Monthly and voluntary contribution tracking
- Benefit calculations for retirement, disability, death, and withdrawals
- Payment processing through Paystack and Flutterwave
- Analytics and reporting in PDF, Excel, and CSV formats

The architecture includes 40+ REST APIs, JWT authentication with role-based access control, Redis caching for performance, and Kafka for async event processing. I implemented everything from database schema design to API endpoints and third-party integrations."

---

### **Follow-up Questions:**

#### **Q1: "That's impressive. Let's talk about security. How did you implement authentication?"**

**Your Answer:**
"I implemented JWT-based stateless authentication with Spring Security 6. Here's the flow:

1. User logs in with credentials
2. System validates against database (password hashed with BCrypt)
3. If valid, generate JWT token with user details and roles
4. Client stores token and sends in Authorization header for subsequent requests
5. JwtAuthenticationFilter intercepts requests, validates token, sets SecurityContext

I also implemented role-based access control with 4 roles: ADMIN, MANAGER, MEMBER, and OPERATOR. For example, only ADMINs can approve benefit applications:

```java
@PreAuthorize("hasRole('ADMIN')")
public BenefitApplication approveBenefit(Long benefitId) {
    // Implementation
}
```

The session is stateless, which means the server doesn't store session data - perfect for horizontal scaling."

---

#### **Q2: "Good. Now tell me about a performance issue you encountered and how you solved it."**

**Your Answer:**
"Great question. Initially, when generating member contribution statements, the API was taking 3-4 seconds because we were loading all contributions without any optimization.

The problem was the classic N+1 query issue. For each member, we were making separate queries to fetch contributions.

I solved it in three ways:

**First, query optimization:**
```java
@Query("SELECT m FROM Member m LEFT JOIN FETCH m.contributions WHERE m.id = :id")
Optional<Member> findByIdWithContributions(@Param("id") Long id);
```

**Second, caching frequently accessed data:**
```java
@Cacheable(value = "members", key = "#id")
public Member findById(Long id) { }
```

**Third, pagination for large result sets:**
```java
PageRequest.of(page, 20, Sort.by("contributionDate").descending())
```

These optimizations reduced response time from 3-4 seconds to under 500ms - about a 75% improvement. We also configured Hibernate batch processing which helped with bulk operations."

---

#### **Q3: "Let's talk about database design. How did you structure your database schema?"**

**Your Answer:**
"I designed a normalized schema with 10+ tables. The core entities are:

**Members table:**
- Primary member information
- Status tracking (ACTIVE, INACTIVE, RETIRED, etc.)
- Soft delete support for audit trails

**Contributions table:**
- Linked to members via foreign key
- Tracks amount, date, type (MONTHLY/VOLUNTARY)
- Unique constraint on member_id + year + month to prevent duplicates

**Benefits table:**
- Benefit applications
- Links to members
- Tracks calculated amounts, status (PENDING, APPROVED, DISBURSED)

**Payments table:**
- Payment transactions
- Links to benefits
- Tracks gateway used, status, timestamps

**Key relationships:**
- Member → Contributions (One-to-Many)
- Member → Benefits (One-to-Many)
- Benefit → Payment (One-to-One)
- Member → Employer (Many-to-One)

I used Flyway for version-controlled migrations, so all schema changes are tracked and reproducible across environments."

---

#### **Q4: "How do you handle exceptions in your application?"**

**Your Answer:**
"I use centralized exception handling with @RestControllerAdvice to ensure consistent error responses across all endpoints.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Resource Not Found")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidContributionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidContribution(
            InvalidContributionException ex) {
        // Return 400 Bad Request with specific message
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        logger.error("Unexpected error", ex); // Log stack trace
        // Return generic 500 error to client (don't leak details)
    }
}
```

This approach provides:
- Consistent error format across all APIs
- Proper HTTP status codes
- Detailed logging for debugging
- User-friendly messages (no stack traces to clients)
- Separation of concerns (controllers don't handle exceptions)"

---

#### **Q5: "Tell me about your testing strategy."**

**Your Answer:**
"I use a multi-layered testing approach:

**Unit Tests for Services:**
- Mock dependencies with Mockito
- Test business logic in isolation
- Fast execution

```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepository repository;

    @InjectMocks
    private MemberService service;

    @Test
    void testCreateMember_Success() {
        when(repository.save(any())).thenReturn(member);
        MemberDTO result = service.createMember(request);
        assertNotNull(result);
        verify(repository).save(any());
    }
}
```

**Integration Tests for APIs:**
- Use @SpringBootTest and MockMvc
- Test complete request-response flow
- Verify database changes

**Repository Tests:**
- Use @DataJpaTest
- Test custom queries
- Verify JPA mappings

I aim for 80%+ code coverage on service layer, focusing on critical business logic like benefit calculations, contribution validation, and payment processing."

---

## 🎭 Scenario 2: Problem-Solving Challenge (15 minutes)

### **Interviewer:**
"Let me give you a scenario. We have an application that processes financial transactions. Users are reporting that sometimes they see duplicate transactions in the system, even though they only submitted once. How would you approach debugging and fixing this?"

### **Your Response (Think Out Loud!):**

**Step 1: Gather Information**
"First, I'd want to understand the problem better:
- How often does this happen? (frequency)
- Is it specific to certain users or random?
- What's the time gap between duplicates? (immediate or delayed)
- Are both transactions processed or just recorded?

Let me check the logs to see if we're receiving duplicate requests or if it's an internal issue."

**Step 2: Form Hypotheses**
"Based on common causes, my hypotheses would be:

1. **Client-side:** User double-clicking submit button
2. **Network:** Request timeout causing retry
3. **Race condition:** Concurrent requests hitting the endpoint
4. **Database:** Missing unique constraints
5. **Idempotency:** Lack of idempotency handling"

**Step 3: Investigation**
"I'd investigate in this order:

```bash
# Check application logs
grep "transaction" logs/application.log | grep "duplicate"

# Check database for actual duplicates
SELECT transaction_id, user_id, amount, created_at, COUNT(*)
FROM transactions
WHERE created_at > NOW() - INTERVAL 7 DAY
GROUP BY transaction_id, user_id, amount, created_at
HAVING COUNT(*) > 1;

# Check request logs for timing
tail -f access.log | grep "POST /api/transactions"
```

**Step 4: Reproduce**
"Try to reproduce in a test environment:
- Rapid double-click on submit
- Concurrent requests using a tool like JMeter
- Network interruption during request"

**Step 5: Solution**
"Depending on the root cause, I'd implement:

**A. Idempotency Key Pattern:**
```java
@PostMapping("/transactions")
public ResponseEntity<?> createTransaction(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody TransactionRequest request) {

    // Check if we've seen this idempotency key before
    Optional<Transaction> existing = transactionRepository
        .findByIdempotencyKey(idempotencyKey);

    if (existing.isPresent()) {
        return ResponseEntity.ok(existing.get()); // Return existing
    }

    // Process new transaction
    Transaction transaction = processTransaction(request);
    transaction.setIdempotencyKey(idempotencyKey);
    return ResponseEntity.created(...).body(transaction);
}
```

**B. Database Constraint:**
```sql
ALTER TABLE transactions
ADD CONSTRAINT unique_transaction
UNIQUE (user_id, amount, reference_number);
```

**C. Distributed Lock (for concurrent requests):**
```java
@Transactional
public Transaction createTransaction(TransactionRequest request) {
    String lockKey = "transaction:" + request.getUserId();

    try {
        if (redisLock.acquire(lockKey, 5000)) {
            // Check for duplicates
            // Process transaction
        }
    } finally {
        redisLock.release(lockKey);
    }
}
```

**D. Frontend Prevention:**
```javascript
// Disable button on submit
button.disabled = true;
// Re-enable after response
```

**Step 6: Testing**
"After implementing the fix, I'd:
- Write test cases for duplicate scenarios
- Load test with concurrent requests
- Monitor in production for recurrence"

**Step 7: Prevention**
"For future prevention:
- Implement all financial endpoints with idempotency
- Add monitoring alerts for duplicate detection
- Document the pattern for other developers"

---

## 🎭 Scenario 3: System Design Question (20 minutes)

### **Interviewer:**
"We need to build a notification system that sends emails to thousands of users daily. The system needs to handle:
- Welcome emails when users register
- Daily digest emails
- Transaction alerts
- Marketing campaigns

How would you design this system?"

### **Your Response:**

"Great question! Let me break this down into components:

**Requirements Clarification:**
- Scale: How many users? (Let's assume 100K+)
- Volume: How many emails per day? (Let's assume 50K+)
- Latency: Real-time for alerts, batch for digests
- Reliability: Critical for transaction alerts, less for marketing
- Rate limits: Email provider limits (e.g., SendGrid: 100 emails/second)

**High-Level Architecture:**

```
┌─────────────────┐
│   Application   │ (Registration, Transactions, etc.)
└────────┬────────┘
         │ Publish Event
         ▼
┌─────────────────┐
│  Message Queue  │ (Kafka/RabbitMQ)
│   (Email Topic) │
└────────┬────────┘
         │ Consume
         ▼
┌─────────────────┐
│ Email Service   │ (Multiple workers)
│   (Workers)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Email Provider │ (SendGrid, AWS SES)
│   (SMTP/API)    │
└─────────────────┘
```

**Component Details:**

**1. Event Publishing (Application Layer):**
```java
@Service
public class UserService {
    @Autowired
    private EmailEventPublisher eventPublisher;

    @Transactional
    public User registerUser(RegistrationRequest request) {
        User user = userRepository.save(new User(request));

        // Publish email event asynchronously
        EmailEvent event = EmailEvent.builder()
            .type(EmailType.WELCOME)
            .recipientEmail(user.getEmail())
            .recipientName(user.getName())
            .priority(Priority.HIGH)
            .build();

        eventPublisher.publish(event);

        return user;
    }
}
```

**2. Message Queue (Kafka):**
```java
@Service
public class EmailEventPublisher {
    @Autowired
    private KafkaTemplate<String, EmailEvent> kafkaTemplate;

    public void publish(EmailEvent event) {
        // Topic based on priority
        String topic = event.getPriority() == Priority.HIGH
            ? "email-high-priority"
            : "email-normal";

        kafkaTemplate.send(topic, event.getRecipientEmail(), event);
    }
}
```

**3. Email Workers (Multiple Consumers):**
```java
@Service
public class EmailWorker {
    private final EmailTemplateService templateService;
    private final EmailProviderClient emailClient;

    @KafkaListener(
        topics = "email-high-priority",
        concurrency = "5" // 5 parallel consumers
    )
    public void processHighPriorityEmails(EmailEvent event) {
        try {
            // Get template
            String htmlContent = templateService.render(
                event.getType(),
                event.getData()
            );

            // Send via provider
            emailClient.send(EmailRequest.builder()
                .to(event.getRecipientEmail())
                .subject(event.getSubject())
                .htmlBody(htmlContent)
                .build()
            );

            // Log success
            emailLogRepository.save(new EmailLog(event, Status.SENT));

        } catch (Exception e) {
            // Handle failure (retry logic)
            handleFailure(event, e);
        }
    }

    @KafkaListener(
        topics = "email-normal",
        concurrency = "10"
    )
    public void processNormalEmails(EmailEvent event) {
        // Similar implementation with rate limiting
    }
}
```

**4. Rate Limiting & Batching:**
```java
@Service
public class RateLimitedEmailSender {
    private final RateLimiter rateLimiter;

    public RateLimitedEmailSender() {
        // 100 emails per second
        this.rateLimiter = RateLimiter.create(100.0);
    }

    public void send(EmailRequest request) {
        rateLimiter.acquire(); // Blocks if over limit
        emailProviderClient.send(request);
    }
}
```

**5. Retry Logic:**
```java
@Retryable(
    value = {EmailProviderException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000, multiplier = 2)
)
public void sendWithRetry(EmailRequest request) {
    emailClient.send(request);
}

@Recover
public void recoverFromEmailFailure(EmailProviderException e, EmailRequest request) {
    // After 3 failures, log to dead letter queue
    deadLetterQueue.send(request);
    alertingService.notify("Email sending failed after retries");
}
```

**6. Database Schema:**
```sql
CREATE TABLE email_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id VARCHAR(255) UNIQUE,
    recipient_email VARCHAR(255),
    email_type VARCHAR(50),
    status VARCHAR(20), -- QUEUED, SENT, FAILED, BOUNCED
    sent_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_status ON email_logs(status, created_at);
CREATE INDEX idx_email_recipient ON email_logs(recipient_email);
```

**7. Monitoring & Observability:**
```java
@Service
public class EmailMetricsService {
    private final MeterRegistry meterRegistry;

    public void recordEmailSent(EmailType type) {
        meterRegistry.counter("emails.sent", "type", type.name()).increment();
    }

    public void recordEmailFailed(EmailType type, String reason) {
        meterRegistry.counter("emails.failed",
            "type", type.name(),
            "reason", reason
        ).increment();
    }
}
```

**Scalability Considerations:**

1. **Horizontal Scaling:** Add more email worker instances
2. **Multiple Queues:** Separate by priority (high/normal/low)
3. **Load Balancing:** Kafka partitions distribute load
4. **Caching:** Template caching to reduce rendering time
5. **Database:** Archive old logs to keep queries fast

**Handling Large Campaigns:**

For marketing campaigns to 100K users:

```java
@Service
public class CampaignService {
    public void sendCampaign(Campaign campaign) {
        // Don't load all users into memory
        int batchSize = 1000;
        int page = 0;

        while (true) {
            Page<User> users = userRepository.findSubscribed(
                PageRequest.of(page, batchSize)
            );

            if (users.isEmpty()) break;

            // Publish in batches
            users.forEach(user -> {
                emailEventPublisher.publish(
                    createCampaignEmail(campaign, user)
                );
            });

            page++;
            Thread.sleep(100); // Rate limiting between batches
        }
    }
}
```

**Cost Optimization:**

- Use cheaper providers for marketing (SendGrid, Mailgun)
- Use transactional providers for critical emails (AWS SES)
- Bulk pricing negotiations
- Bounce handling to avoid sending to invalid emails

This architecture handles:
✅ High throughput (thousands of emails)
✅ Reliability (retry logic, DLQ)
✅ Scalability (horizontal scaling)
✅ Priority handling (separate queues)
✅ Monitoring (metrics, logs)
✅ Rate limiting (provider limits)"

---

## 🎭 Scenario 4: Behavioral Questions (15 minutes)

### **Q1: Tell me about a time you disagreed with a technical decision. How did you handle it?**

**Good Answer (STAR Format):**

**Situation:**
"In my pension management project, I initially decided to use MongoDB as the database because I thought the flexible schema would be beneficial for evolving requirements."

**Task:**
"However, after researching pension fund regulations, I realized the data has strict relationships and ACID compliance requirements - members must have contributions, benefits must be linked to specific members, and financial transactions need strong consistency."

**Action:**
"I reconsidered my decision and switched to MySQL because:
- Pension data has clear relationships (members, contributions, benefits)
- Financial transactions require ACID properties
- Regulatory compliance needs audit trails (referential integrity)
- Complex reporting queries are easier with SQL joins

I documented my analysis comparing MongoDB vs MySQL, including pros/cons for our specific use case, and made the switch early in development."

**Result:**
"This turned out to be the right decision. The relational model made benefit calculations much cleaner, and Flyway migrations gave us version-controlled schema changes. When I needed to add a unique constraint to prevent duplicate contributions, it was straightforward with SQL. The project succeeded with MySQL, and I learned the importance of choosing technology based on requirements, not trends."

---

### **Q2: Describe a situation where you had to learn something quickly under pressure.**

**Good Answer:**

**Situation:**
"While building the pension system, I needed to integrate payment gateways (Paystack and Flutterwave) for benefit disbursements. I had never worked with payment APIs before and had a 2-week deadline."

**Task:**
"I needed to understand payment gateway integration, handle webhooks for payment confirmations, manage different payment statuses, and implement proper error handling for financial transactions."

**Action:**
"I structured my learning:

**Week 1 - Learning:**
- Read official Paystack documentation
- Built a simple proof-of-concept in a separate project
- Tested in sandbox mode with test cards
- Joined their Slack community to ask questions

**Week 2 - Implementation:**
- Created a PaymentGateway interface for abstraction
- Implemented Paystack integration first (most popular in Nigeria)
- Added comprehensive error handling and logging
- Implemented webhook verification for security
- Added Flutterwave as second provider using same interface

```java
public interface PaymentGateway {
    PaymentResponse initiatePayment(PaymentRequest request);
    PaymentStatus verifyPayment(String reference);
    boolean validateWebhook(String signature, String payload);
}
```

I also set up thorough logging and monitoring because payment systems can't fail silently."

**Result:**
"Completed the integration in 1.5 weeks, leaving buffer time for testing. The abstraction made it easy to add Flutterwave support. I learned that:
- Breaking down complex problems helps (POC first, then production code)
- Good documentation is invaluable
- Financial integrations require extra care with error handling
- The Strategy pattern makes it easy to support multiple providers

The payment system has been working reliably, and I'm now confident with third-party API integrations."

---

### **Q3: Tell me about a time you made a mistake. How did you handle it?**

**Good Answer:**

**Situation:**
"Early in the pension project, I implemented soft deletes for members but forgot to add the @Where clause to filter out deleted records in queries."

**Task:**
"This meant deleted members were still appearing in member lists, contribution reports, and analytics - causing confusion and incorrect statistics."

**Action:**
"As soon as I realized the mistake during testing:

1. **Immediate fix:**
```java
@Entity
@Table(name = "members")
@Where(clause = "deleted = false")
@SQLDelete(sql = "UPDATE members SET deleted = true WHERE id = ?")
public class Member {
    private Boolean deleted = false;
}
```

2. **Verified impact:** Checked which queries were affected
3. **Tested thoroughly:** Ensured soft delete worked across all endpoints
4. **Added test cases:**
```java
@Test
void testDeletedMembersNotIncludedInFindAll() {
    member.setDeleted(true);
    memberRepository.save(member);

    List<Member> activeMembers = memberRepository.findAll();
    assertFalse(activeMembers.contains(member));
}
```

5. **Documented the pattern:** Added comments explaining soft delete implementation for future reference."

**Result:**
"The fix worked perfectly. More importantly, I learned:
- Test edge cases early (what happens when records are deleted?)
- Soft deletes require consistent implementation across the codebase
- Write tests for business-critical features immediately
- Don't assume - verify behavior

This experience made me more thorough in my testing approach and I now always consider the complete lifecycle of data (create, read, update, delete, soft delete) when designing features."

---

## 🎭 Scenario 5: Code Review Exercise (10 minutes)

### **Interviewer:**
"I'm going to show you some code. Tell me what issues you see and how you'd improve it."

```java
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepo.findAll();
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        user.setPassword(user.getPassword()); // Store password
        return userRepo.save(user);
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepo.findById(id).get();
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepo.deleteById(id);
    }
}
```

### **Your Code Review Response:**

"I see several issues with this code. Let me go through them:

**1. Security Issue - Password Not Encrypted:**
```java
// PROBLEM: Storing plain text password
user.setPassword(user.getPassword());

// FIX: Encrypt password
@Autowired
private PasswordEncoder passwordEncoder;

user.setPassword(passwordEncoder.encode(user.getPassword()));
```

**2. Missing Request Mapping Path:**
```java
// PROBLEM: No base path
@RestController
public class UserController {

// FIX: Add base path and versioning
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
```

**3. No Service Layer (Direct Repository Access):**
```java
// PROBLEM: Controller directly using repository
@Autowired
private UserRepository userRepo;

// FIX: Use service layer
@Autowired
private UserService userService;

@GetMapping
public ResponseEntity<Page<UserDTO>> getUsers(Pageable pageable) {
    return ResponseEntity.ok(userService.findAll(pageable));
}
```

**4. Exposing Internal Entities (No DTOs):**
```java
// PROBLEM: Returning JPA entities
public List<User> getUsers()

// FIX: Use DTOs
public ResponseEntity<List<UserDTO>> getUsers() {
    List<UserDTO> users = userService.findAll()
        .stream()
        .map(userMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(users);
}
```

**5. No Validation:**
```java
// PROBLEM: No input validation
@PostMapping("/users")
public User createUser(@RequestBody User user)

// FIX: Add validation
@PostMapping
public ResponseEntity<UserDTO> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    UserDTO user = userService.createUser(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .header("Location", "/api/v1/users/" + user.getId())
        .body(user);
}

// DTO with validation
public class CreateUserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

**6. No Error Handling:**
```java
// PROBLEM: .get() throws NoSuchElementException
return userRepo.findById(id).get();

// FIX: Proper error handling
return userRepo.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException(
        "User not found with id: " + id));
```

**7. No Pagination (Performance Issue):**
```java
// PROBLEM: Returns all users (could be millions)
public List<User> getUsers() {
    return userRepo.findAll();
}

// FIX: Add pagination
@GetMapping
public Page<UserDTO> getUsers(
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
    return userService.findAll(pageable);
}
```

**8. Inconsistent HTTP Methods:**
```java
// PROBLEM: DELETE returns void
public void deleteUser(@PathVariable Long id)

// FIX: Proper HTTP response
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build(); // 204 No Content
}
```

**9. No Security/Authorization:**
```java
// Missing: Who can access these endpoints?

// FIX: Add method security
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id)
```

**10. Field Injection (Bad Practice):**
```java
// PROBLEM: Field injection
@Autowired
private UserRepository userRepo;

// FIX: Constructor injection
private final UserService userService;

public UserController(UserService userService) {
    this.userService = userService;
}
```

**Improved Version:**

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<UserDTO> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/users/" + user.getId())
            .body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

This addresses all the issues: security, validation, proper layering, DTOs, error handling, pagination, and proper HTTP semantics."

---

## 📝 Practice Tips

### **For Each Scenario:**

1. **Read the question completely** before answering
2. **Ask clarifying questions** if anything is unclear
3. **Think out loud** - show your thought process
4. **Use the STAR method** for behavioral questions
5. **Reference your pension system** when relevant
6. **Be specific** - give code examples, not just theory
7. **Admit what you don't know** but show how you'd learn

### **Common Mistakes to Avoid:**

❌ Rambling without structure
❌ Jumping to solutions without understanding the problem
❌ Being vague ("I would make it better")
❌ Saying "I don't know" without following up
❌ Badmouthing technologies or previous work
❌ Over-promising skills you don't have

### **What to Do Instead:**

✅ Structure your answers (Problem → Analysis → Solution → Result)
✅ Ask clarifying questions
✅ Be specific with examples
✅ Show learning agility ("I haven't used X, but I learned Y quickly by...")
✅ Be honest and humble
✅ Show enthusiasm for learning

---

## 🎯 Final Mock Interview Checklist

Practice these scenarios:
- [ ] Project overview (30-second and 2-minute versions)
- [ ] Technical deep dive on security
- [ ] Performance optimization story
- [ ] Database design explanation
- [ ] Problem-solving walkthrough
- [ ] System design question
- [ ] Behavioral STAR responses
- [ ] Code review exercise

**Record yourself** practicing these scenarios to improve delivery!

**Good luck! You're well-prepared! 🚀**
