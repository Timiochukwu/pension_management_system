# Payment Gateway Integration (Paystack & Flutterwave)

## Overview
This document explains the payment gateway integration for the Pension Management System, supporting both **Paystack** and **Flutterwave**.

## 🏗️ Architecture

### Components Created:
1. **Entities & Enums**
   - `PaymentGateway` enum - PAYSTACK, FLUTTERWAVE, MANUAL
   - `PaymentStatus` enum - Complete payment lifecycle
   - `Payment` entity - Tracks all payment transactions

2. **Repository**
   - `PaymentRepository` - Database operations for payments

3. **DTOs**
   - `InitializePaymentRequest` - Start a payment
   - `PaymentResponse` - Payment details response

4. **Mapper**
   - `PaymentMapper` - Entity ↔ DTO conversion

5. **Service** (Interface defined, implementation needed)
   - `PaymentService` - Payment operations contract

## 📋 Implementation Status

### ✅ Completed:
- Payment entity with comprehensive tracking fields
- Payment gateway and status enums with detailed documentation
- Payment repository with custom queries
- Payment DTOs for API communication
- Payment mapper for object conversion
- Service interface defining all operations

### 🔨 To Implement:
- `PaymentServiceImpl` - Actual service implementation
- Paystack API integration methods
- Flutterwave API integration methods
- `PaymentController` - REST API endpoints
- Webhook handling for both gateways

## 🚀 How to Complete Implementation

### Step 1: Add Dependencies to `pom.xml`

```xml
<!-- For HTTP requests to payment gateways -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- For JSON processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

### Step 2: Add Configuration to `application.properties`

```properties
# Paystack Configuration
paystack.secret.key=sk_test_your_paystack_secret_key
paystack.public.key=pk_test_your_paystack_public_key
paystack.base.url=https://api.paystack.co

# Flutterwave Configuration
flutterwave.secret.key=FLWSECK_TEST-your_flutterwave_secret_key
flutterwave.public.key=FLWPUBK_TEST-your_flutterwave_public_key
flutterwave.base.url=https://api.flutterwave.com/v3

# Callback URLs
payment.callback.base.url=https://yourapp.com/api/v1/payments
```

### Step 3: Paystack Integration

#### Initialize Payment (Paystack):
```java
POST https://api.paystack.co/transaction/initialize
Headers:
  Authorization: Bearer sk_test_xxx
  Content-Type: application/json

Body:
{
  "email": "member@example.com",
  "amount": "1000000", // Amount in kobo (₦10,000 = 1,000,000 kobo)
  "reference": "PMT-20250115-ABC123",
  "callback_url": "https://yourapp.com/api/v1/payments/callback"
}

Response:
{
  "status": true,
  "message": "Authorization URL created",
  "data": {
    "authorization_url": "https://checkout.paystack.com/abc123",
    "access_code": "abc123",
    "reference": "PMT-20250115-ABC123"
  }
}
```

#### Verify Payment (Paystack):
```java
GET https://api.paystack.co/transaction/verify/:reference
Headers:
  Authorization: Bearer sk_test_xxx

Response:
{
  "status": true,
  "message": "Verification successful",
  "data": {
    "id": 1234567890,
    "status": "success",
    "reference": "PMT-20250115-ABC123",
    "amount": 1000000,
    "paid_at": "2025-01-15T10:30:45.000Z",
    "channel": "card"
  }
}
```

### Step 4: Flutterwave Integration

#### Initialize Payment (Flutterwave):
```java
POST https://api.flutterwave.com/v3/payments
Headers:
  Authorization: Bearer FLWSECK_TEST-xxx
  Content-Type: application/json

Body:
{
  "tx_ref": "PMT-20250115-ABC123",
  "amount": "10000",
  "currency": "NGN",
  "redirect_url": "https://yourapp.com/api/v1/payments/callback",
  "customer": {
    "email": "member@example.com"
  },
  "customizations": {
    "title": "Pension Contribution",
    "description": "Monthly contribution payment"
  }
}

Response:
{
  "status": "success",
  "message": "Hosted Link",
  "data": {
    "link": "https://checkout.flutterwave.com/v3/hosted/pay/xyz789"
  }
}
```

#### Verify Payment (Flutterwave):
```java
GET https://api.flutterwave.com/v3/transactions/:id/verify
Headers:
  Authorization: Bearer FLWSECK_TEST-xxx

Response:
{
  "status": "success",
  "message": "Transaction fetched successfully",
  "data": {
    "id": 1234567,
    "tx_ref": "PMT-20250115-ABC123",
    "amount": 10000,
    "status": "successful",
    "payment_type": "card"
  }
}
```

## 🔐 Webhook Security

### Paystack Webhook Verification:
```java
// Verify signature
String signature = request.getHeader("x-paystack-signature");
String computedSignature = HmacSHA512(webhookPayload, secretKey);
if (!signature.equals(computedSignature)) {
    throw new SecurityException("Invalid webhook signature");
}
```

### Flutterwave Webhook Verification:
```java
// Verify secret hash
String secretHash = request.getHeader("verif-hash");
if (!secretHash.equals(flutterwaveSecretHash)) {
    throw new SecurityException("Invalid webhook signature");
}
```

## 📊 Payment Flow

```
1. User creates contribution
   ↓
2. User clicks "Pay Now"
   ↓
3. Frontend calls POST /api/v1/payments/initialize
   {
     "contributionId": 123,
     "amount": 10000,
     "gateway": "PAYSTACK",
     "email": "member@example.com"
   }
   ↓
4. Backend:
   - Creates payment record (status: INITIATED)
   - Calls Paystack/Flutterwave initialize API
   - Stores authorization URL
   - Returns payment response
   ↓
5. Frontend redirects user to authorization_url
   ↓
6. User completes payment on Paystack/Flutterwave
   ↓
7. Gateway sends webhook to /api/v1/payments/webhook/{gateway}
   ↓
8. Backend:
   - Verifies webhook signature
   - Calls gateway verify API
   - Updates payment status to SUCCESS
   - Updates contribution status to COMPLETED
   ↓
9. Gateway redirects user back to callback_url
   ↓
10. Frontend shows success message
```

## 🧪 Testing

### Test Credentials:
- **Paystack Test Cards**: https://paystack.com/docs/payments/test-payments
  - Success: 4084084084084081
  - Decline: 5060666666666666666

- **Flutterwave Test Cards**: https://developer.flutterwave.com/docs/integration-guides/testing-helpers
  - Success: 5531886652142950
  - CVV: 564, PIN: 3310

## 📁 Files Created

1. `/payment/entity/PaymentGateway.java` - Gateway enum
2. `/payment/entity/PaymentStatus.java` - Status enum
3. `/payment/entity/Payment.java` - Payment entity
4. `/payment/repository/PaymentRepository.java` - Repository
5. `/payment/dto/InitializePaymentRequest.java` - Request DTO
6. `/payment/dto/PaymentResponse.java` - Response DTO
7. `/payment/mapper/PaymentMapper.java` - Mapper
8. `/payment/service/PaymentService.java` - Service interface

## 🎯 Next Steps

1. Implement `PaymentServiceImpl` with actual API calls
2. Create `PaymentController` with endpoints
3. Add webhook endpoints for both gateways
4. Add payment gateway configuration beans
5. Test with test credentials
6. Add payment retry logic
7. Implement payment refunds
8. Add payment analytics

## 📚 Resources

- Paystack Docs: https://paystack.com/docs
- Flutterwave Docs: https://developer.flutterwave.com
- Webhook Testing: https://webhook.site

---

**Status**: Foundation complete, ready for API integration implementation
