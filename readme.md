# Pension Management System

A comprehensive Spring Boot application for managing pension funds, member contributions, benefits, and employer relationships.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Deployment](#deployment)
- [Testing](#testing)
- [Contributing](#contributing)

## ✨ Features

### Core Modules

#### 1. Member Management
- Complete member registration and profile management
- Status tracking (ACTIVE, INACTIVE, SUSPENDED, RETIRED, TERMINATED)
- Age and eligibility validation
- Soft delete functionality
- Member analytics and reporting

#### 2. Contribution Processing
- Monthly and voluntary contribution types
- Duplicate monthly contribution prevention
- Contribution validation (minimum amounts, active member status)
- Contribution history and statements
- Payment method tracking
- Total contribution calculations

#### 3. Benefit Calculation & Processing
- Multiple benefit types:
  - Retirement benefits (age 60+, 5 years service)
  - Voluntary withdrawals
  - Death benefits
  - Disability benefits
  - Partial withdrawals (25% limit)
- Automatic benefit calculations including:
  - Employer contributions (10%)
  - Investment returns (8% annual)
  - Tax deductions (10%)
  - Administrative fees (2%)
- Complete workflow: Application → Review → Approval → Disbursement
- Automatic member status updates

#### 4. Employer Management
- Employer registration and management
- Employee roster tracking
- Company verification

#### 5. Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Roles: ADMIN, MANAGER, MEMBER, OPERATOR
- Password encryption with BCrypt
- Stateless session management

#### 6. Reporting & Analytics
- System-wide statistics dashboard
- Member-specific analytics
- Export reports in multiple formats:
  - CSV (Comma-Separated Values)
  - Excel (XLSX)
  - PDF (Portable Document Format)
- Contribution summaries
- Benefit calculations

#### 7. Background Jobs (Quartz Scheduler)
- Monthly contribution reminders
- Retirement eligibility notifications
- Pending benefit alerts
- Monthly system reports

#### 8. Email Notifications
- Member registration confirmations
- Contribution receipts
- Benefit application updates
- Approval notifications
- Async email processing

## 🛠 Tech Stack

### Backend
- **Java 22** with preview features
- **Spring Boot 3.5.6**
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM
- **MapStruct 1.6.2** - DTO mapping
- **Quartz Scheduler** - Background jobs

### Database
- **MySQL 8.0+** (Primary)
- **PostgreSQL** support available

### Security
- **JWT (jjwt 0.11.5)** - Token-based authentication
- **BCrypt** - Password encryption

### Documentation
- **SpringDoc OpenAPI 3.0** - API documentation
- **Swagger UI** - Interactive API testing

### Build & Testing
- **Maven 3** - Build automation
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing

### File Processing
- **Apache POI** - Excel generation
- **iText PDF** - PDF generation
- **OpenCSV** - CSV export

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **GitHub Actions** - CI/CD pipeline

## 🏗 Architecture

The application follows **Clean Architecture** principles with clear separation of concerns:

```
src/main/java/pension_management_system/pension/
├── auth/               # Authentication & user management
├── member/             # Member management
├── contribution/       # Contribution processing
├── benefit/            # Benefit calculation & processing
├── employer/           # Employer management
├── analytics/          # Analytics & statistics
├── report/             # Report generation & export
├── notification/       # Email notification service
├── jobs/               # Background scheduled jobs
└── common/             # Shared utilities & exceptions
```

### Layer Structure
- **Controller** - REST API endpoints
- **Service** - Business logic
- **Repository** - Data access
- **Entity** - JPA entities
- **DTO** - Data Transfer Objects
- **Mapper** - Entity-DTO mapping

## 🚀 Getting Started

### Prerequisites
- JDK 22 or higher
- Maven 3.6+
- MySQL 8.0+ or PostgreSQL
- Docker & Docker Compose (for containerized deployment)

### Local Development Setup

#### 1. Clone the repository
```bash
git clone https://github.com/Timiochukwu/pension_management_system.git
cd pension_management_system
```

#### 2. Configure Database
Create a MySQL database:
```sql
CREATE DATABASE java_pension_management_system;
```

Update `.env` file or set environment variables:
```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=java_pension_management_system
DB_USERNAME=root
DB_PASSWORD=your_password
```

#### 3. Build the application
```bash
./mvnw clean install
```

#### 4. Run the application
```bash
./mvnw spring-boot:run
```

Or with specific profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 5. Access the application
- **Application**: http://localhost:1110
- **Swagger UI**: http://localhost:1110/swagger-ui.html
- **API Docs**: http://localhost:1110/v3/api-docs

### Docker Deployment

#### Using Docker Compose (Recommended)
```bash
# Start all services (app + database + adminer)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

#### Manual Docker Build
```bash
# Build image
docker build -t pension-management-system .

# Run container
docker run -p 1110:1110 \
  -e DB_HOST=host.docker.internal \
  -e DB_NAME=java_pension_management_system \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=your_password \
  pension-management-system
```

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "MEMBER"
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "johndoe",
    "email": "john@example.com",
    "role": "MEMBER",
    "expiresIn": 86400000
  }
}
```

### Member Endpoints

All member endpoints require authentication. Include JWT token in header:
```
Authorization: Bearer <your_jwt_token>
```

#### Register Member
```http
POST /api/v1/members
Content-Type: application/json
Authorization: Bearer <token>

{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "phoneNumber": "+2348012345678",
  "dateOfBirth": "1985-03-15",
  "address": "123 Main Street",
  "city": "Lagos",
  "state": "Lagos",
  "country": "Nigeria"
}
```

### Contribution Endpoints

#### Process Contribution
```http
POST /api/v1/contributions
Authorization: Bearer <token>

{
  "memberId": 1,
  "contributionType": "MONTHLY",
  "contributionAmount": 5000.00,
  "contributionDate": "2024-11-15",
  "paymentMethod": "BANK_TRANSFER",
  "description": "November 2024 contribution"
}
```

#### Get Member Contributions
```http
GET /api/v1/contributions/member/1
Authorization: Bearer <token>
```

#### Generate Contribution Statement
```http
GET /api/v1/contributions/member/1/statement?startDate=2024-01-01&endDate=2024-12-31
Authorization: Bearer <token>
```

### Benefit Endpoints

#### Calculate Benefit
```http
GET /api/v1/benefits/calculate/1?benefitType=RETIREMENT
Authorization: Bearer <token>
```

#### Apply for Benefit
```http
POST /api/v1/benefits
Authorization: Bearer <token>

{
  "memberId": 1,
  "benefitType": "RETIREMENT",
  "paymentMethod": "BANK_TRANSFER",
  "accountNumber": "1234567890",
  "bankName": "First Bank",
  "remarks": "Retirement application"
}
```

#### Approve Benefit (ADMIN/MANAGER only)
```http
PUT /api/v1/benefits/1/approve?approvedBy=admin
Authorization: Bearer <token>
```

### Report Export Endpoints

#### Export Members Report
```http
GET /api/v1/reports/members/export?format=EXCEL
Authorization: Bearer <token>
```

Supported formats: `CSV`, `EXCEL`, `PDF`

#### Export System Statistics
```http
GET /api/v1/reports/statistics/export?format=PDF
Authorization: Bearer <token>
```

## 🔐 Security & Roles

### Role Permissions

| Endpoint Pattern | ADMIN | MANAGER | MEMBER | OPERATOR |
|-----------------|-------|---------|--------|----------|
| `/auth/**` | ✅ | ✅ | ✅ | ✅ |
| `/members/**` | ✅ | ✅ | ✅ own data | ❌ |
| `/contributions/**` | ✅ | ✅ | ✅ own data | ✅ |
| `/employers/**` | ✅ | ✅ | ❌ | ❌ |
| `/benefits/calculate/**` | ✅ | ✅ | ✅ own data | ❌ |
| `/benefits/*/approve` | ✅ | ✅ | ❌ | ❌ |
| `/benefits/**` | ✅ | ✅ | ✅ own data | ✅ |
| `/analytics/**` | ✅ | ✅ | ❌ | ❌ |
| `/reports/**` | ✅ | ✅ | ❌ | ❌ |

## 🧪 Testing

See [TESTING.md](TESTING.md) for detailed testing documentation.

### Run Tests
```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=MemberServiceImplTest

# With coverage
./mvnw clean test jacoco:report
```

### Test Coverage
- **Target**: 70% minimum
- **Current**: Unit tests for Member, Contribution, and Benefit services
- Coverage reports: `target/site/jacoco/index.html`

## 📦 Deployment

### Environment Variables

Required environment variables for production:

```bash
# Database
DB_HOST=production-db-host
DB_PORT=3306
DB_NAME=pension_prod
DB_USERNAME=prod_user
DB_PASSWORD=secure_password

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000

# Email (Optional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Profile
SPRING_PROFILES_ACTIVE=prod
```

### Production Checklist

- [ ] Update JWT secret to secure random key
- [ ] Configure production database credentials
- [ ] Enable SSL/TLS for database connections
- [ ] Set up email service credentials
- [ ] Configure Redis for caching (optional)
- [ ] Set up monitoring and logging
- [ ] Configure backup strategy
- [ ] Review security configurations
- [ ] Enable CORS for frontend domain
- [ ] Set up rate limiting

## 🔄 Background Jobs

The system runs scheduled jobs using Quartz Scheduler:

| Job | Schedule | Description |
|-----|----------|-------------|
| Contribution Reminder | 1st of month, 9:00 AM | Reminds members without monthly contributions |
| Retirement Eligibility | Daily, 2:00 AM | Notifies members approaching retirement |
| Pending Benefit Reminder | Weekly, Monday 10:00 AM | Alerts admins of pending benefits (>7 days) |
| Monthly Report | Last day of month, 11:59 PM | Generates system statistics report |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Java naming conventions
- Use Lombok annotations appropriately
- Write comprehensive JavaDoc for public methods
- Maintain test coverage above 70%

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **Timiochukwu** - Initial development

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- MapStruct for seamless DTO mapping
- All contributors and users of this system

## 📞 Support

For support, email support@pensionsystem.com or create an issue in the GitHub repository.

---

Built with ❤️ using Spring Boot
