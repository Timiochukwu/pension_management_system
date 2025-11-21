# Pension Management System - Complete File Structure

## Project Root Files
```
/home/user/pension_management_system/
├── pom.xml                          # Maven configuration
├── readme.md                         # Quick start guide
├── CODEBASE_ANALYSIS.md            # Comprehensive analysis (NEW)
├── PROJECT_STATUS.txt              # Quick status summary (NEW)
├── FILE_STRUCTURE.md               # This file
├── mvnw / mvnw.cmd                 # Maven wrapper
├── .gitignore / .gitattributes     # Git configuration
├── CRITICAL_FEATURES.md            # Features documentation
├── SENIOR_JAVA_SKILLS_ALIGNMENT.md # Skills mapping
├── PAYMENT_INTEGRATION_README.md   # Payment gateway guide
├── required.md                      # Requirements
├── file_structure.txt               # Original structure
└── src/                            # Source code directory
```

---

## Source Code Structure

### Main Application Entry Point
```
src/main/java/pension_management_system/pension/
└── PensionApplication.java         # Spring Boot main application class
```

### 1. MEMBER MODULE
```
src/main/java/pension_management_system/pension/member/
├── entity/
│   ├── Member.java                 # Main member entity with all properties
│   └── MemberStatus.java           # Enum: ACTIVE, INACTIVE, SUSPENDED, RETIRED
├── repository/
│   └── MemberRepository.java       # JpaRepository with custom queries
├── service/
│   ├── MemberService.java          # Service interface
│   └── impl/
│       └── MemberServiceImpl.java   # Service implementation
├── controller/
│   └── MemberController.java       # REST endpoints: /api/v1/members/*
├── mapper/
│   └── MemberMapper.java           # MapStruct entity-DTO mapper
├── dto/
│   ├── MemberRequest.java          # Request DTO for registration
│   └── MemberResponse.java         # Response DTO
└── specification/
    └── MemberSpecification.java    # JPA Criteria for advanced filtering
```

### 2. CONTRIBUTION MODULE
```
src/main/java/pension_management_system/pension/contribution/
├── entity/
│   ├── Contribution.java           # Main contribution entity
│   ├── ContributionType.java       # Enum: MONTHLY, VOLUNTARY
│   ├── ContributionStatus.java     # Enum: PENDING, COMPLETED, FAILED
│   └── PaymentMethod.java          # Enum: BANK_TRANSFER, CARD, etc.
├── repository/
│   └── ContributionRepository.java # Custom queries for contributions
├── service/
│   ├── ContributionService.java    # Service interface
│   └── impl/
│       └── ContributionServiceImpl.java # Service implementation
├── controller/
│   └── ContributionController.java # REST endpoints: /api/v1/contributions/*
├── mapper/
│   └── ContributionMapper.java     # MapStruct mapper
├── dto/
│   ├── ContributionRequest.java    # Request DTO
│   ├── ContributionResponse.java   # Response DTO
│   └── ContributionStatementResponse.java # Statement DTO
├── specification/
│   └── ContributionSpecification.java # Advanced filtering
```

### 3. EMPLOYER MODULE
```
src/main/java/pension_management_system/pension/employer/
├── entity/
│   └── Employer.java               # Employer entity
├── repository/
│   └── EmployerRepository.java     # Custom queries
├── service/
│   ├── EmployerService.java        # Service interface
│   └── impl/
│       └── EmployerServiceImpl.java # Service implementation
├── controller/
│   └── EmployerController.java     # REST endpoints: /api/v1/employers/*
├── mapper/
│   └── EmployerMapper.java         # MapStruct mapper
├── dto/
│   ├── EmployerRequest.java        # Request DTO
│   └── EmployerResponse.java       # Response DTO
├── specification/
│   └── EmployerSpecification.java  # Advanced filtering
```

### 4. BENEFIT MODULE
```
src/main/java/pension_management_system/pension/benefit/
├── entity/
│   ├── Benefit.java                # Benefit entity
│   ├── BenefitType.java            # Enum: RETIREMENT, DISABILITY, SURVIVOR
│   └── BenefitStatus.java          # Enum: PENDING, APPROVED, PAID, REJECTED
├── repository/
│   └── BenefitRepository.java      # Custom queries
├── service/
│   ├── BenefitService.java         # Service interface
│   └── impl/
│       └── BenefitServiceImpl.java  # Service implementation
├── controller/
│   └── BenefitController.java      # REST endpoints: /api/v1/benefits/*
├── mapper/
│   └── BenefitMapper.java          # MapStruct mapper
└── dto/
    ├── BenefitRequest.java         # Request DTO
    └── BenefitResponse.java        # Response DTO
```

### 5. PAYMENT MODULE (Extended)
```
src/main/java/pension_management_system/pension/payment/
├── entity/
│   ├── Payment.java                # Payment entity
│   ├── PaymentStatus.java          # Enum: PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
│   └── PaymentGateway.java         # Enum: PAYSTACK, FLUTTERWAVE
├── repository/
│   └── PaymentRepository.java      # Custom queries
├── service/
│   ├── PaymentService.java         # Service interface
│   ├── impl/
│   │   └── PaymentServiceImpl.java  # Service implementation
│   └── gateway/
│       ├── PaystackService.java    # Paystack gateway integration
│       └── FlutterwaveService.java # Flutterwave gateway integration
├── controller/
│   └── PaymentController.java      # REST endpoints: /api/v1/payments/*
├── mapper/
│   └── PaymentMapper.java          # MapStruct mapper
└── dto/
    ├── InitializePaymentRequest.java
    └── PaymentResponse.java
```

### 6. SECURITY & AUTHENTICATION MODULE
```
src/main/java/pension_management_system/pension/security/
├── AuthController.java             # Login/Register endpoints: /api/auth/*
├── JwtUtil.java                    # JWT token generation and validation
├── JwtAuthenticationFilter.java    # Spring Security filter
├── Role.java                       # Enum: ADMIN, USER, MEMBER
├── entity/
│   └── User.java                   # User entity
├── repository/
│   └── UserRepository.java         # User queries
└── service/
    ├── UserService.java            # User management service
    └── CustomUserDetailsService.java # Spring Security user details provider
```

### 7. NOTIFICATION & EMAIL MODULE
```
src/main/java/pension_management_system/pension/notification/
├── event/
│   ├── MemberRegisteredEvent.java  # Event when member registers
│   ├── ContributionCreatedEvent.java # Event when contribution created
│   └── PaymentSuccessEvent.java    # Event when payment succeeds
├── listener/
│   └── EmailEventListener.java     # Event listener that sends emails
└── service/
    ├── EmailService.java           # Email sending interface
    └── TemplateEmailService.java   # Thymeleaf template email service
```

### 8. VERIFICATION MODULE (BVN)
```
src/main/java/pension_management_system/pension/verification/
├── entity/
│   └── BvnVerification.java        # BVN verification record
├── repository/
│   └── BvnVerificationRepository.java
├── service/
│   └── BvnVerificationService.java # BVN API integration
├── controller/
│   └── BvnVerificationController.java # REST endpoints
└── dto/
    ├── BvnVerificationRequest.java
    └── BvnVerificationResponse.java
```

### 9. WEBHOOK MODULE
```
src/main/java/pension_management_system/pension/webhook/
├── entity/
│   ├── Webhook.java                # Webhook subscription entity
│   ├── WebhookEvent.java           # Enum of webhook events
│   └── WebhookDelivery.java        # Delivery attempt tracking
├── repository/
│   ├── WebhookRepository.java
│   └── WebhookDeliveryRepository.java
├── service/
│   └── WebhookService.java         # Webhook event and delivery management
├── controller/
│   └── WebhookController.java      # REST endpoints
└── dto/
    ├── WebhookRequest.java
    └── WebhookResponse.java
```

### 10. ANALYTICS MODULE
```
src/main/java/pension_management_system/pension/analytics/
├── service/
│   ├── AnalyticsService.java       # Service interface
│   └── impl/
│       └── AnalyticsServiceImpl.java
├── controller/
│   └── AnalyticsController.java    # REST endpoints: /api/v1/analytics/*
└── dto/
    ├── DashboardStatisticsResponse.java
    ├── ContributionTrendResponse.java
    ├── MemberStatusDistribution.java
    ├── ContributionByPaymentMethod.java
    ├── RecentActivityResponse.java
    └── TopEmployersResponse.java
```

### 11. REPORT MODULE
```
src/main/java/pension_management_system/pension/report/
├── entity/
│   ├── Report.java                 # Report entity
│   ├── ReportType.java             # Enum: MEMBER, CONTRIBUTION, FINANCIAL, etc.
│   └── ReportFormat.java           # Enum: PDF, EXCEL, CSV
├── repository/
│   └── ReportRepository.java
├── service/
│   ├── ReportService.java          # Service interface
│   └── ReportServiceImpl.java       # Service implementation
├── controller/
│   └── ReportController.java       # REST endpoints
├── mapper/
│   └── ReportMapper.java
└── dto/
    ├── ReportRequest.java
    └── ReportResponse.java
```

### 12. EXPORT MODULE
```
src/main/java/pension_management_system/pension/export/
├── service/
│   ├── ExportService.java          # Service interface
│   └── impl/
│       └── ExportServiceImpl.java   # Handles PDF, Excel, CSV export
├── controller/
│   └── ExportController.java       # REST endpoints
└── dto/
    └── ExportFormat.java           # Enum: PDF, EXCEL, CSV
```

### 13. UPLOAD MODULE
```
src/main/java/pension_management_system/pension/upload/
├── service/
│   ├── UploadService.java          # Service interface
│   └── impl/
│       └── UploadServiceImpl.java   # Batch upload processing
├── controller/
│   └── UploadController.java       # REST endpoints
└── dto/
    └── UploadResultResponse.java
```

### 14. MACHINE LEARNING MODULE
```
src/main/java/pension_management_system/pension/ml/
├── service/
│   ├── FraudDetectionService.java  # Fraud detection interface
│   ├── PaymentFraudDetector.java   # Fraud detection implementation
│   └── RiskAssessmentService.java  # Risk assessment service
├── controller/
│   └── MLController.java           # REST endpoints: /api/v1/ml/*
└── dto/
    ├── FraudDetectionRequest.java
    ├── FraudDetectionResponse.java
    └── RiskAssessmentResponse.java
```

### 15. AUDIT MODULE
```
src/main/java/pension_management_system/pension/audit/
├── entity/
│   ├── AuditLog.java               # Audit log entity
│   └── AuditAction.java            # Enum of actions
├── repository/
│   └── AuditLogRepository.java
└── service/
    └── AuditService.java           # Audit logging service
```

### 16. MONITORING MODULE
```
src/main/java/pension_management_system/pension/monitoring/
├── health/
│   ├── DatabaseHealthIndicator.java   # Custom DB health check
│   ├── PaymentGatewayHealthIndicator.java # Gateway health check
│   └── RedisHealthIndicator.java   # Redis health check
├── service/
│   └── MetricsService.java         # Metrics collection
├── scheduler/
│   └── MetricsUpdateScheduler.java # Updates metrics every minute
└── config/
    └── MonitoringConfig.java       # Monitoring configuration
```

### 17. CACHING MODULE
```
src/main/java/pension_management_system/pension/cache/
├── CacheWarmingService.java        # Cache initialization
└── DistributedRateLimiter.java    # Rate limiting with Redis
```

### 18. SCHEDULER MODULE (Background Jobs)
```
src/main/java/pension_management_system/pension/scheduler/
└── ScheduledTasks.java             # 5 scheduled jobs with cron expressions
```

### 19. EXCEPTION HANDLING
```
src/main/java/pension_management_system/pension/exception/
├── GlobalExceptionHandler.java     # Centralized exception handler
├── BusinessException.java          # Base custom exception
├── DuplicateResourceException.java
├── ResourceNotFoundException.java
└── ErrorResponse.java              # Error response DTO
```

```
src/main/java/pension_management_system/pension/common/exception/
├── MemberNotFoundException.java
├── DuplicateMonthlyContributionException.java
└── InvalidContributionException.java
```

### 20. CONFIGURATION CLASSES
```
src/main/java/pension_management_system/pension/config/
├── SecurityConfig.java             # Spring Security configuration
├── RedisConfig.java                # Redis/Jedis configuration
├── KafkaConfig.java                # Kafka message broker
├── WebClientConfig.java            # HTTP client setup
├── RateLimitConfig.java            # Rate limiting configuration
├── SessionConfig.java              # Session management
├── CorsConfig.java                 # CORS configuration
└── SwaggerConfig.java              # OpenAPI/Swagger documentation
```

```
src/main/java/pension_management_system/pension/common/config/
└── SecurityConfig.java (may have additional config)
```

### 21. DTOs (Common)
```
src/main/java/pension_management_system/pension/common/dto/
├── ApiResponseDto.java             # Standard API response wrapper
└── ErrorResponse.java              # Standard error response
```

---

## Resources

### Configuration Files
```
src/main/resources/
├── application.properties           # Main Spring Boot configuration
├── application-monitoring.properties # Monitoring-specific config
└── db/migration/                   # Flyway database migrations
```

### Database Migrations (Flyway)
```
src/main/resources/db/migration/
├── V1__Create_audit_logs_table.sql
├── V2__Create_users_table.sql
├── V3__Create_members_table.sql
├── V4__Create_employers_table.sql
├── V5__Create_contributions_table.sql
├── V6__Create_benefits_table.sql
├── V7__Create_payments_table.sql
├── V8__Create_reports_table.sql
├── V9__Create_webhooks_tables.sql
└── V10__Create_bvn_verifications_table.sql
```

### Email Templates
```
src/main/resources/templates/emails/
├── welcome-email.html              # Welcome email template
├── contribution-confirmation.html  # Contribution confirmation email
└── payment-success.html            # Payment success notification email
```

---

## Test Files

### Unit Tests (Excluded from Build)
```
src/test/java/pension_management_system/pension/
├── PensionApplicationTests.java
├── benefit/service/BenefitServiceImplTest.java
├── contribution/service/ContributionServiceImplTest.java
├── member/service/MemberServiceImplTest.java
└── payment/
    ├── controller/PaymentControllerTest.java
    ├── repository/PaymentRepositoryTest.java
    └── service/PaymentServiceImplTest.java
```

---

## Maven Build Configuration
- **Java Version:** 22 (with preview features)
- **Spring Boot Parent:** 3.5.6
- **Key Plugins:**
  - maven-compiler-plugin (Java 22)
  - maven-surefire-plugin (Test runner)
  - spring-boot-maven-plugin (Package executable JAR)

---

## File Statistics

| Category | Count |
|----------|-------|
| Total Java Files | 148 |
| Entity Classes | 20+ |
| Repository Interfaces | 15+ |
| Service Classes | 30+ |
| Controller Classes | 15+ |
| DTO Classes | 40+ |
| Mapper Classes | 10+ |
| Configuration Classes | 8+ |
| Exception Classes | 8+ |
| Database Migrations | 10 |
| Test Files | 7 |
| Email Templates | 3 |

---

## Key Absolute Paths

```
Core Application:
  /home/user/pension_management_system/src/main/java/pension_management_system/pension/PensionApplication.java

Configuration:
  /home/user/pension_management_system/src/main/resources/application.properties
  /home/user/pension_management_system/src/main/java/.../config/SecurityConfig.java

Documentation:
  /home/user/pension_management_system/CODEBASE_ANALYSIS.md
  /home/user/pension_management_system/PROJECT_STATUS.txt
  /home/user/pension_management_system/readme.md

Database Migrations:
  /home/user/pension_management_system/src/main/resources/db/migration/

Tests:
  /home/user/pension_management_system/src/test/java/pension_management_system/pension/
```

---

## Quick Navigation

To find a specific file, use the module name:
- Member management → `src/main/java/.../member/`
- Contribution processing → `src/main/java/.../contribution/`
- Payment handling → `src/main/java/.../payment/`
- Security/Auth → `src/main/java/.../security/`
- Background jobs → `src/main/java/.../scheduler/`
- Database setup → `src/main/resources/db/migration/`
- Configuration → `src/main/java/.../config/`

