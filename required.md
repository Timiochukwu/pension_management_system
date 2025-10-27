Pension Contribution Management System - Java Assessment
Assessment Overview
This technical assessment evaluates candidates' proficiency in Java Spring Boot, Spring Data JPA, PostgreSQL/MySQL, and related technologies, with a focus on pension system development.
Project Description
Create a simplified pension contribution management system with the following features:

Member Registration and Management
Contribution Processing
Background Job Processing
API Documentation
Unit Tests

Technical Requirements
Architecture & Design

Implement Clean Architecture principles (Hexagonal Architecture)
Use Spring Boot 3.x or later
Implement Repository pattern (Spring Data JPA)
Include Domain-Driven Design (DDD) concepts
Use Spring Data JPA for data access
Implement proper dependency injection (Spring IoC)

Core Features
1. Member Management API

Create new member
Update member details
Retrieve member information
Delete member (soft delete)

2. Contribution and Benefit Processing
   Handle two types of contributions:

Monthly mandatory contributions (limited to one per month)
Voluntary contributions (multiple allowed per month)

Features:

Calculate total contributions by type
Generate contribution statements
Basic benefit calculation based on contribution history

Business rules:

Monthly contributions must be within the same calendar month
Voluntary contributions can be made any time
Benefits are calculated based on total contribution value
Minimum contribution period before benefit eligibility

3. Business Rules and Background Processing
   Key business rules:

Monthly contribution validation
Benefit eligibility checks
Contribution type restrictions

Implement Spring Scheduler or Quartz for scheduled jobs:

Monthly contribution validation reports
Benefit eligibility updates
Simple monthly interest calculations
Generate member statements

Error handling and retry mechanism:

Failed contribution processing
Invalid transaction rollback
Notification for failed processes

4. Data Model and Validation
   Member entity with Bean Validation (JSR-380) rules:

Required fields: firstName, lastName, dateOfBirth, email
Age must be between 18 and 70 years
Valid email format
Phone number format validation

Contribution entity with validation:

contributionType (MONTHLY/VOLUNTARY)
amount (greater than 0)
contributionDate validation:

Monthly contributions: Only one per calendar month
Voluntary: No date restrictions


Reference number format validation

Employer entity with validation:

Required fields: companyName, registrationNumber
Valid business registration format
Active status validation

Benefit entity:

benefitType
calculationDate
eligibilityStatus
amount

Transaction history tracking all changes
Technical Implementation Requirements
1. Validation and Business Rules

Implement Bean Validation (javax.validation / jakarta.validation)
Create custom validators for:

Monthly contribution date checks
Benefit eligibility rules
Member age validation


Separate business rules into dedicated service classes
Use validation with @Valid annotations
Implement proper error messages and codes

2. API Development

RESTful API design
Proper HTTP status codes
Request/Response DTOs (using MapStruct or ModelMapper)
Input validation
Exception handling with @ControllerAdvice
API versioning

3. Database

Spring Data JPA with Hibernate
Liquibase or Flyway migrations
Proper relationship mapping (@OneToMany, @ManyToOne, etc.)
Indexed queries for performance
Soft delete implementation using @SQLDelete and @Where

4. Background Jobs

Spring @Scheduled or Quartz Scheduler implementation
Recurring job setup
Error handling and logging

5. Testing

Unit tests using JUnit 5 and Mockito (minimum 70% coverage)
Integration tests for critical paths using @SpringBootTest
Mock repository implementation
TestContainers for database integration tests (bonus)

Technology Stack
Required

Java: 17 or later
Framework: Spring Boot 3.x
Build Tool: Maven or Gradle
Database: PostgreSQL or MySQL
ORM: Spring Data JPA with Hibernate
Validation: Bean Validation (Hibernate Validator)
Scheduler: Spring @Scheduled or Quartz
Testing: JUnit 5, Mockito, Spring Boot Test
API Documentation: SpringDoc OpenAPI (Swagger)

Recommended Libraries

Logging: SLF4J with Logback
Mapping: MapStruct or ModelMapper
Migration: Liquibase or Flyway
Lombok: For reducing boilerplate code

Deliverables

Source code in a public GitHub repository
README.md with:

Project setup instructions
API documentation
Architecture overview
Design decisions explanation
How to run the application
Environment variables/configuration needed


Database migration scripts (Liquibase or Flyway)
Postman collection or OpenAPI/Swagger documentation for API testing
application.properties / application.yml example configuration

