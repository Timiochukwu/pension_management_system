# Technology Deep Dive Questions
## Detailed Technical Questions for Each Technology

---

## ☕ Java 22 Questions

### **Q1: What new features in modern Java have you used?**

**Answer:**
"In my pension system with Java 22, I've used several modern features:

**1. Records (Java 14+):**
```java
public record ContributionStatementDTO(
    Long memberId,
    String memberName,
    BigDecimal totalContributions,
    List<ContributionDTO> contributions,
    LocalDate generatedAt
) {}
```
Records are perfect for immutable DTOs - concise syntax, automatic equals/hashCode, and clear intent.

**2. Pattern Matching for instanceof (Java 16+):**
```java
public void processBenefit(Benefit benefit) {
    if (benefit instanceof RetirementBenefit rb) {
        // rb is automatically cast
        processRetirementBenefit(rb.getServiceYears());
    } else if (benefit instanceof DisabilityBenefit db) {
        processDisabilityBenefit(db.getDisabilityLevel());
    }
}
```

**3. Text Blocks (Java 15+):**
```java
String emailTemplate = """
    Dear %s,

    Your contribution of %s has been received.

    Total contributions: %s

    Thank you,
    Pension Management Team
    """.formatted(memberName, amount, total);
```

**4. Stream API enhancements:**
```java
// Calculate total contributions using streams
BigDecimal total = contributions.stream()
    .map(Contribution::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// Filter and collect
List<Member> retirees = members.stream()
    .filter(m -> m.getAge() >= 60)
    .filter(m -> m.getYearsOfService() >= 5)
    .collect(Collectors.toList());
```

**5. Optional for null safety:**
```java
public MemberDTO findById(Long id) {
    return memberRepository.findById(id)
        .map(memberMapper::toDTO)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Member not found with id: " + id));
}
```

**6. var for local variables:**
```java
var contributions = contributionRepository.findByMemberId(memberId);
var totalAmount = calculateTotal(contributions);
```"

---

### **Q2: Explain the difference between equals() and == in Java**

**Answer:**
"**==** compares object references (memory addresses), while **equals()** compares object values.

```java
// Example from pension system
String email1 = "john@example.com";
String email2 = "john@example.com";
String email3 = new String("john@example.com");

email1 == email2        // true (string pool - same reference)
email1 == email3        // false (different objects)
email1.equals(email3)   // true (same value)

// For entities
Member member1 = memberRepository.findById(1L).orElseThrow();
Member member2 = memberRepository.findById(1L).orElseThrow();

member1 == member2      // false (different JPA entity instances)
member1.equals(member2) // true (if equals() checks ID)
```

**Best practice in JPA entities:**
```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return id != null && id.equals(member.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

For entities, I override equals() to compare by ID, ensuring consistent behavior across JPA session boundaries."

---

### **Q3: What is the difference between List, Set, and Map?**

**Answer:**
"All are Collection interfaces with different characteristics:

**List** - Ordered, allows duplicates
```java
List<Contribution> contributions = new ArrayList<>();
contributions.add(new Contribution(...));
contributions.add(new Contribution(...)); // Can add duplicates
contributions.get(0); // Access by index
```

**Set** - Unordered (HashSet) or sorted (TreeSet), no duplicates
```java
Set<String> memberEmails = new HashSet<>();
memberEmails.add("john@example.com");
memberEmails.add("john@example.com"); // Ignored (duplicate)
// memberEmails.size() == 1
```

**Map** - Key-value pairs, unique keys
```java
Map<Long, Member> memberCache = new HashMap<>();
memberCache.put(1L, member1);
memberCache.put(2L, member2);
Member m = memberCache.get(1L);
```

**Real-world usage in pension system:**

```java
// List - when order matters and duplicates allowed
List<Contribution> monthlyContributions = member.getContributions();

// Set - for unique collections
@Entity
public class Employer {
    @OneToMany
    private Set<Member> employees; // Each member appears once
}

// Map - for caching and lookups
@Service
public class PaymentService {
    private final Map<String, PaymentGateway> gateways = Map.of(
        "PAYSTACK", paystackGateway,
        "FLUTTERWAVE", flutterwaveGateway
    );

    public PaymentResponse process(String gateway, PaymentRequest request) {
        return gateways.get(gateway).processPayment(request);
    }
}
```"

---

### **Q4: What is a lambda expression? Give an example.**

**Answer:**
"Lambda expressions are anonymous functions that implement functional interfaces concisely.

**Syntax:** `(parameters) -> expression` or `(parameters) -> { statements; }`

**Examples from pension system:**

**1. Stream operations:**
```java
// Before Java 8 (verbose)
List<String> emails = new ArrayList<>();
for (Member member : members) {
    if (member.getStatus() == MemberStatus.ACTIVE) {
        emails.add(member.getEmail());
    }
}

// With lambdas (concise)
List<String> emails = members.stream()
    .filter(member -> member.getStatus() == MemberStatus.ACTIVE)
    .map(member -> member.getEmail())
    .collect(Collectors.toList());
```

**2. Sorting:**
```java
// Sort contributions by date (descending)
contributions.sort((c1, c2) ->
    c2.getContributionDate().compareTo(c1.getContributionDate()));

// Or using method reference
contributions.sort(Comparator.comparing(Contribution::getContributionDate)
    .reversed());
```

**3. Custom functional interfaces:**
```java
@FunctionalInterface
public interface BenefitCalculator {
    BigDecimal calculate(Member member, List<Contribution> contributions);
}

BenefitCalculator retirementCalculator = (member, contributions) -> {
    BigDecimal total = contributions.stream()
        .map(Contribution::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal employerContribution = total.multiply(new BigDecimal("0.10"));
    return total.add(employerContribution);
};

BigDecimal benefit = retirementCalculator.calculate(member, contributions);
```

**4. Exception handling in streams:**
```java
members.forEach(member -> {
    try {
        sendEmail(member);
    } catch (EmailException e) {
        logger.error("Failed to send email to {}", member.getEmail(), e);
    }
});
```"

---

## 🍃 Spring Boot Questions

### **Q5: What is Dependency Injection? Explain with example.**

**Answer:**
"Dependency Injection (DI) is a design pattern where objects receive their dependencies from external sources rather than creating them internally. Spring IoC container manages this.

**Without DI (tight coupling):**
```java
public class MemberService {
    private MemberRepository repository = new MemberRepositoryImpl(); // Tight coupling!

    public Member findById(Long id) {
        return repository.findById(id);
    }
}
```
Problems: Hard to test, can't swap implementations, violates SOLID principles.

**With DI (loose coupling):**
```java
@Service
public class MemberService {
    private final MemberRepository repository;

    // Constructor injection (recommended)
    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    public Member findById(Long id) {
        return repository.findById(id).orElseThrow();
    }
}
```

**Types of DI in Spring:**

**1. Constructor Injection (BEST PRACTICE):**
```java
@Service
@RequiredArgsConstructor // Lombok generates constructor
public class BenefitService {
    private final BenefitRepository benefitRepository;
    private final BenefitCalculationService calculationService;
    private final MemberService memberService;
    private final NotificationService notificationService;

    // Spring auto-wires all dependencies
}
```

**2. Field Injection (NOT RECOMMENDED):**
```java
@Service
public class PaymentService {
    @Autowired
    private PaymentRepository repository; // Hard to test, hides dependencies
}
```

**3. Setter Injection (for optional dependencies):**
```java
@Service
public class EmailService {
    private TemplateEngine templateEngine;

    @Autowired(required = false)
    public void setTemplateEngine(TemplateEngine engine) {
        this.templateEngine = engine;
    }
}
```

**Benefits in my pension system:**
- Easy testing (inject mocks)
- Loose coupling
- Single Responsibility Principle
- Swap implementations without changing code

**Example - Testing with DI:**
```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepository repository; // Mock dependency

    @InjectMocks
    private MemberService service; // Auto-inject mocks

    @Test
    void testFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(member));
        MemberDTO result = service.findById(1L);
        assertNotNull(result);
    }
}
```"

---

### **Q6: What is the difference between @Component, @Service, @Repository, and @Controller?**

**Answer:**
"All are stereotypes that mark classes for Spring component scanning, but they have semantic differences:

**@Component** - Generic Spring-managed bean
```java
@Component
public class EmailTemplateRenderer {
    public String render(String template, Map<String, Object> data) {
        // Template rendering logic
    }
}
```

**@Service** - Business logic layer
```java
@Service
public class BenefitCalculationService {
    // Business logic for calculating benefits
    public BigDecimal calculateRetirementBenefit(Member member) {
        // Complex calculation logic
    }
}
```

**@Repository** - Data access layer (adds persistence exception translation)
```java
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
```

**@Controller / @RestController** - Presentation layer
```java
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {
    // Handles HTTP requests
}
```

**Key differences:**

| Annotation | Layer | Special Features |
|------------|-------|------------------|
| @Component | Generic | None |
| @Service | Business | None (semantic only) |
| @Repository | Data | Exception translation (SQLException → DataAccessException) |
| @Controller | Presentation | Request mapping support |
| @RestController | Presentation | @Controller + @ResponseBody (automatic JSON) |

**Why use specific annotations?**

1. **Clarity** - Immediately understand the layer
2. **Exception Translation** - @Repository converts checked SQLExceptions
3. **Future enhancements** - Spring may add layer-specific features
4. **AOP pointcuts** - Can target specific layers

**Layered architecture in pension system:**
```
┌─────────────────────────────────────┐
│  @RestController (MemberController) │  ← HTTP layer
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│  @Service (MemberService)           │  ← Business logic
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│  @Repository (MemberRepository)     │  ← Data access
└─────────────────────────────────────┘
```"

---

### **Q7: Explain @Transactional and when to use it**

**Answer:**
"@Transactional ensures a method executes within a database transaction - all operations succeed or all fail (ACID properties).

**Basic usage:**
```java
@Service
public class BenefitApplicationService {

    @Transactional
    public BenefitApplication processBenefitApplication(Long applicationId) {
        // Step 1: Update application status
        BenefitApplication app = benefitRepository.findById(applicationId)
            .orElseThrow();
        app.setStatus(BenefitStatus.APPROVED);

        // Step 2: Create payment
        Payment payment = createPayment(app);
        paymentRepository.save(payment);

        // Step 3: Update member status
        Member member = app.getMember();
        member.setStatus(MemberStatus.RETIRED);
        memberRepository.save(member);

        // Step 4: Send notification
        notificationService.sendApprovalEmail(member);

        // If ANY step fails, ALL changes roll back
        return benefitRepository.save(app);
    }
}
```

**Transaction propagation:**
```java
// REQUIRED (default) - Join existing transaction or create new
@Transactional(propagation = Propagation.REQUIRED)
public void method1() { }

// REQUIRES_NEW - Always create new transaction (suspend current)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void auditLog(String action) {
    // This commits even if parent transaction fails
}

// SUPPORTS - Use transaction if exists, non-transactional otherwise
@Transactional(propagation = Propagation.SUPPORTS)
public Member findById(Long id) { }
```

**Isolation levels:**
```java
// READ_COMMITTED (default) - Prevents dirty reads
@Transactional(isolation = Isolation.READ_COMMITTED)
public void recordContribution() { }

// SERIALIZABLE - Strictest (prevents dirty reads, non-repeatable reads, phantom reads)
@Transactional(isolation = Isolation.SERIALIZABLE)
public void disburseBenefit(Long benefitId) {
    // Ensure no concurrent modifications
}
```

**Read-only optimization:**
```java
@Transactional(readOnly = true)
public List<MemberDTO> generateReport() {
    // Optimization: Hibernate won't flush changes, can route to read replica
    return memberRepository.findAll().stream()
        .map(memberMapper::toDTO)
        .collect(Collectors.toList());
}
```

**Rollback configuration:**
```java
// Default: Rolls back on RuntimeException, not checked exceptions
@Transactional

// Rollback on specific exceptions
@Transactional(rollbackFor = {InvalidDataException.class, IOException.class})

// Don't rollback on specific exceptions
@Transactional(noRollbackFor = {ValidationException.class})
```

**Real-world example from pension system:**

**Problem:** Recording contribution and sending email
```java
// BAD - If email fails, contribution is lost
public void recordContribution(ContributionRequest request) {
    Contribution contribution = contributionRepository.save(new Contribution(request));
    emailService.sendReceipt(contribution); // Email fails!
    // Contribution already committed to database
}
```

**Solution:** Separate concerns
```java
@Transactional
public Contribution recordContribution(ContributionRequest request) {
    Contribution contribution = contributionRepository.save(new Contribution(request));

    // Publish event instead of direct email
    eventPublisher.publishContributionCreated(contribution);

    return contribution;
}

// Email sent asynchronously by event listener
@Async
@EventListener
public void handleContributionCreated(ContributionCreatedEvent event) {
    emailService.sendReceipt(event.getContribution());
    // If email fails, contribution is still saved
}
```

**When to use @Transactional:**
✅ Multiple database operations that must succeed/fail together
✅ Updating related entities
✅ Complex business operations requiring consistency
✅ Financial transactions

**When NOT to use:**
❌ Simple read operations (use readOnly=true if needed)
❌ Methods with external API calls (keep transactions short)
❌ Long-running operations (locks database resources)"

---

## 🔐 Spring Security Questions

### **Q8: How does JWT authentication work in Spring Security?**

**Answer:**
"JWT (JSON Web Token) is a stateless authentication mechanism. Here's my implementation:

**Flow:**
```
1. User logs in with credentials
2. Server validates and generates JWT
3. Client stores JWT (localStorage/cookie)
4. Client sends JWT in Authorization header for subsequent requests
5. Server validates JWT and authorizes request
```

**Implementation:**

**1. JWT Service - Token Generation:**
```java
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration; // 24 hours

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
```

**2. JWT Authentication Filter:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // 2. Extract username from token
        String username = jwtService.extractUsername(token);

        // 3. If username exists and not already authenticated
        if (username != null &&
            SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Validate token
            if (jwtService.validateToken(token, userDetails)) {
                // 6. Create authentication object
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 8. Continue filter chain
        filterChain.doFilter(request, response);
    }
}
```

**3. Security Configuration:**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Stateless, no CSRF needed
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Public endpoints
                .requestMatchers("/swagger-ui/**").permitAll()
                .anyRequest().authenticated() // Everything else requires auth
            )
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class); // Add JWT filter

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength: 12 rounds
    }
}
```

**4. Authentication Controller:**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        // 2. Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        // 3. Return token
        return ResponseEntity.ok(LoginResponse.builder()
            .token(token)
            .type("Bearer")
            .expiresIn(24 * 60 * 60) // 24 hours in seconds
            .build());
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

**5. Method-level Security:**
```java
@RestController
@RequestMapping("/api/v1/benefits")
public class BenefitController {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<BenefitDTO> approveBenefit(@PathVariable Long id) {
        // Only users with ADMIN role can access
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<BenefitDTO>> getAllBenefits() {
        // ADMIN or MANAGER can access
    }

    @PreAuthorize("#memberId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BenefitDTO>> getMemberBenefits(@PathVariable Long memberId) {
        // User can only see their own benefits, or ADMIN can see all
    }
}
```

**Benefits of JWT:**
✅ Stateless (no server-side session storage)
✅ Scalable (works across multiple servers)
✅ Self-contained (token has all user info)
✅ Cross-domain (CORS-friendly)

**Security considerations:**
- Secret key stored in environment variables (not in code)
- Token expiration (24 hours)
- HTTPS only in production
- Refresh token mechanism for long-lived sessions
- Token blacklist for logout (if needed)"

---

## 🗄️ Database & JPA Questions

### **Q9: Explain the N+1 query problem and how to solve it**

**Answer:**
"The N+1 problem occurs when you load N entities, then execute N additional queries to load their associations.

**Problem Example:**
```java
// Load all members (1 query)
List<Member> members = memberRepository.findAll();

// For each member, load contributions (N queries!)
for (Member member : members) {
    List<Contribution> contributions = member.getContributions(); // Lazy loading!
    System.out.println(member.getName() + ": " + contributions.size());
}

// Total: 1 + N queries (if 100 members = 101 queries!)
```

**SQL generated:**
```sql
-- Query 1
SELECT * FROM members;

-- Query 2 (for member 1)
SELECT * FROM contributions WHERE member_id = 1;

-- Query 3 (for member 2)
SELECT * FROM contributions WHERE member_id = 2;

-- ... 100 more queries!
```

**Solutions:**

**1. JOIN FETCH (Eager loading):**
```java
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.contributions WHERE m.id = :id")
    Optional<Member> findByIdWithContributions(@Param("id") Long id);

    @Query("SELECT DISTINCT m FROM Member m LEFT JOIN FETCH m.contributions")
    List<Member> findAllWithContributions();
}
```

**SQL generated:**
```sql
-- Single query with JOIN!
SELECT m.*, c.*
FROM members m
LEFT JOIN contributions c ON c.member_id = m.id;
```

**2. Entity Graph:**
```java
@Entity
@NamedEntityGraph(
    name = "Member.contributions",
    attributeNodes = @NamedAttributeNode("contributions")
)
public class Member {
    @OneToMany(mappedBy = "member")
    private List<Contribution> contributions;
}

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @EntityGraph(value = "Member.contributions")
    List<Member> findAll();
}
```

**3. Batch Fetching:**
```java
@Entity
public class Member {
    @OneToMany(mappedBy = "member")
    @BatchSize(size = 10)
    private List<Contribution> contributions;
}
```

**Configuration:**
```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=10
```

Now instead of N queries, we get N/10 queries (batching).

**4. Projection (if you only need specific fields):**
```java
public interface MemberSummary {
    Long getId();
    String getFullName();
    BigDecimal getTotalContributions();
}

@Query("SELECT m.id as id, m.fullName as fullName, " +
       "SUM(c.amount) as totalContributions " +
       "FROM Member m LEFT JOIN m.contributions c " +
       "GROUP BY m.id, m.fullName")
List<MemberSummary> findMemberSummaries();
```

**Real-world example from pension system:**

**Before optimization (N+1 problem):**
```java
@GetMapping("/members/report")
public List<MemberReportDTO> generateReport() {
    List<Member> members = memberRepository.findAll(); // 1 query

    return members.stream()
        .map(member -> MemberReportDTO.builder()
            .name(member.getFullName())
            .contributionCount(member.getContributions().size()) // N queries!
            .totalContributions(calculateTotal(member.getContributions()))
            .build())
        .collect(Collectors.toList());
}

// For 1000 members: 1001 queries! 🐌
```

**After optimization:**
```java
@GetMapping("/members/report")
public List<MemberReportDTO> generateReport() {
    // Single query with projection
    return memberRepository.findMemberReportData();
}

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query(\"\"\"
        SELECT new com.pension.dto.MemberReportDTO(
            m.fullName,
            COUNT(c.id),
            COALESCE(SUM(c.amount), 0)
        )
        FROM Member m
        LEFT JOIN m.contributions c
        GROUP BY m.id, m.fullName
        \"\"\")
    List<MemberReportDTO> findMemberReportData();
}

// For 1000 members: 1 query! 🚀
```

**Result:** Reduced query time from 5 seconds to 200ms (25x improvement)!"

---

### **Q10: Explain database transactions isolation levels**

**Answer:**
"Isolation levels determine how transaction changes are visible to other concurrent transactions.

**Problems without proper isolation:**

1. **Dirty Read** - Reading uncommitted changes from another transaction
2. **Non-repeatable Read** - Reading same row twice gets different results
3. **Phantom Read** - Reading same query twice gets different rows

**Isolation Levels (from least to most isolated):**

**1. READ_UNCOMMITTED (lowest isolation):**
```java
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public void method() { }
```

- ✅ Allows: Dirty reads, non-repeatable reads, phantom reads
- ⚡ Performance: Best
- 🎯 Use case: Rarely used (data inconsistency risk)

**2. READ_COMMITTED (default in most databases):**
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void processContribution() {
    // Only reads committed data
}
```

- ✅ Prevents: Dirty reads
- ❌ Allows: Non-repeatable reads, phantom reads
- ⚡ Performance: Good
- 🎯 Use case: Most applications (good balance)

**Example:**
```java
// Transaction 1
@Transactional(isolation = Isolation.READ_COMMITTED)
public void updateMemberStatus(Long memberId) {
    Member member = memberRepository.findById(memberId).orElseThrow();
    member.setStatus(MemberStatus.ACTIVE);
    // member is updated but NOT YET COMMITTED

    // Transaction 2 cannot see this change until we commit
}
```

**3. REPEATABLE_READ:**
```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public BigDecimal calculateBenefit(Long memberId) {
    // Same row will return same data within this transaction
}
```

- ✅ Prevents: Dirty reads, non-repeatable reads
- ❌ Allows: Phantom reads
- ⚡ Performance: Moderate (holds read locks)
- 🎯 Use case: Reports, calculations requiring consistency

**4. SERIALIZABLE (highest isolation):**
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void disburseBenefit(Long benefitId) {
    // Complete isolation - like executing sequentially
}
```

- ✅ Prevents: Dirty reads, non-repeatable reads, phantom reads
- ❌ Allows: Nothing (complete isolation)
- ⚡ Performance: Worst (highest locking)
- 🎯 Use case: Critical financial operations

**Real-world examples from pension system:**

**Example 1: Preventing double-disbursement (SERIALIZABLE):**
```java
@Service
public class BenefitDisbursementService {

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Payment disburseBenefit(Long benefitId) {
        BenefitApplication benefit = benefitRepository.findById(benefitId)
            .orElseThrow();

        // Check status
        if (benefit.getStatus() == BenefitStatus.DISBURSED) {
            throw new AlreadyDisbursedException("Benefit already disbursed");
        }

        // Create payment
        Payment payment = processPayment(benefit);

        // Update status
        benefit.setStatus(BenefitStatus.DISBURSED);
        benefit.setDisbursedAt(LocalDateTime.now());

        benefitRepository.save(benefit);

        // SERIALIZABLE ensures no concurrent transaction can also disburse
        return payment;
    }
}
```

**Example 2: Contribution summary report (REPEATABLE_READ):**
```java
@Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
public ContributionSummary generateMonthlySummary(YearMonth month) {
    // First read
    List<Contribution> contributions = contributionRepository
        .findByMonth(month);

    BigDecimal total = contributions.stream()
        .map(Contribution::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Second read (same data as first read, even if other transactions committed)
    Long count = contributionRepository.countByMonth(month);

    return new ContributionSummary(total, count);
}
```

**Example 3: Regular operations (READ_COMMITTED):**
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public Contribution recordContribution(ContributionRequest request) {
    // Standard isolation for normal operations
    Member member = memberRepository.findById(request.getMemberId())
        .orElseThrow();

    Contribution contribution = Contribution.builder()
        .member(member)
        .amount(request.getAmount())
        .build();

    return contributionRepository.save(contribution);
}
```

**Choosing the right isolation level:**

| Scenario | Isolation Level | Reason |
|----------|----------------|---------|
| Reading member list | READ_COMMITTED | Normal read, OK if data changes |
| Generating monthly report | REPEATABLE_READ | Consistent snapshot needed |
| Disbursing benefits | SERIALIZABLE | Prevent duplicate disbursement |
| Recording contribution | READ_COMMITTED | Balance performance & consistency |
| Approving benefit | SERIALIZABLE | Critical state change |

**Performance vs Consistency trade-off:**
```
READ_UNCOMMITTED → READ_COMMITTED → REPEATABLE_READ → SERIALIZABLE
      ↑                                                      ↑
   Fastest                                              Slowest
   Least consistent                               Most consistent
```"

---

## 📨 Kafka & Messaging Questions

### **Q11: How do you use Kafka in your application?**

**Answer:**
"I use Kafka for async event processing - decoupling time-consuming operations from the critical path.

**Architecture:**
```
Application → Kafka Topic → Consumer(s) → Actions (Email, Analytics, etc.)
```

**Implementation:**

**1. Producer (Publishing Events):**
```java
@Service
@RequiredArgsConstructor
public class ContributionEventPublisher {

    private final KafkaTemplate<String, ContributionEvent> kafkaTemplate;

    public void publishContributionCreated(Contribution contribution) {
        ContributionEvent event = ContributionEvent.builder()
            .contributionId(contribution.getId())
            .memberId(contribution.getMember().getId())
            .amount(contribution.getAmount())
            .contributionDate(contribution.getContributionDate())
            .timestamp(LocalDateTime.now())
            .build();

        // Send to topic, partitioned by member ID
        kafkaTemplate.send("contribution-events",
                          contribution.getMember().getId().toString(),
                          event);

        logger.info("Published contribution event: {}", event.getContributionId());
    }
}
```

**2. Consumer (Listening to Events):**
```java
@Service
@Slf4j
public class ContributionEventConsumer {

    private final NotificationService notificationService;
    private final AnalyticsService analyticsService;

    @KafkaListener(
        topics = "contribution-events",
        groupId = "pension-notification-group",
        concurrency = "3" // 3 parallel consumers
    )
    public void handleContributionEvent(ContributionEvent event) {
        log.info("Received contribution event: {}", event.getContributionId());

        try {
            // Send email receipt (time-consuming, now async)
            notificationService.sendContributionReceipt(event);

            // Update analytics (non-critical, async)
            analyticsService.updateMemberStats(event.getMemberId());

        } catch (Exception e) {
            log.error("Error processing contribution event", e);
            // Event will be retried automatically
            throw e;
        }
    }
}
```

**3. Configuration:**
```java
@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, ContributionEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                  StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                  JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Ensure durability
        config.put(ProducerConfig.RETRIES_CONFIG, 3);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public ConsumerFactory<String, ContributionEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "pension-notification-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                  StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                  JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, ContributionEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

**4. Error Handling & Retry:**
```java
@Configuration
public class KafkaErrorConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ContributionEvent>
            kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, ContributionEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Retry configuration
        factory.setCommonErrorHandler(
            new DefaultErrorHandler(
                new FixedBackOff(2000L, 3L) // 3 retries with 2-second delay
            )
        );

        return factory;
    }
}
```

**Use Cases in Pension System:**

**1. Contribution Processing:**
```java
@Transactional
public Contribution recordContribution(ContributionRequest request) {
    // Critical path - save to database
    Contribution contribution = contributionRepository.save(
        new Contribution(request)
    );

    // Non-critical path - publish event (async)
    eventPublisher.publishContributionCreated(contribution);

    return contribution; // Return immediately, email sent later
}
```

**2. Benefit Application:**
```java
@KafkaListener(topics = "benefit-application-events")
public void handleBenefitApplication(BenefitApplicationEvent event) {
    // Multiple consumers can process different aspects
    switch (event.getStatus()) {
        case SUBMITTED:
            notificationService.notifyAdmins(event);
            break;
        case APPROVED:
            notificationService.notifyMember(event);
            paymentService.initiateDisbursement(event);
            break;
        case REJECTED:
            notificationService.sendRejectionEmail(event);
            break;
    }
}
```

**Benefits:**
✅ **Decoupling** - Services don't depend on each other directly
✅ **Scalability** - Add more consumers to handle load
✅ **Resilience** - Messages persisted even if consumers are down
✅ **Async Processing** - Critical path completes quickly
✅ **Event Sourcing** - Audit trail of all events

**Monitoring:**
```java
@Component
public class KafkaMetrics {

    private final MeterRegistry meterRegistry;

    public void recordEventPublished(String topic) {
        meterRegistry.counter("kafka.events.published", "topic", topic)
            .increment();
    }

    public void recordEventProcessed(String topic, boolean success) {
        meterRegistry.counter("kafka.events.processed",
            "topic", topic,
            "status", success ? "success" : "failure"
        ).increment();
    }
}
```"

---

## 🎯 Final Tips for Technical Questions

### **How to Answer Technical Questions:**

1. **Structure your answer:**
   - Define the concept
   - Explain with an example
   - Relate to your project
   - Mention trade-offs/best practices

2. **Use the pension system as reference:**
   - Shows practical application
   - Demonstrates you've solved real problems
   - Makes answers concrete, not theoretical

3. **Admit when unsure:**
   - "I haven't used X extensively, but my understanding is..."
   - "I'd need to research that, but here's my approach..."
   - Shows honesty and learning mindset

4. **Ask clarifying questions:**
   - "Are you asking about X or Y specifically?"
   - Shows thoughtfulness

**Good luck! 🚀 You know your stuff!**
