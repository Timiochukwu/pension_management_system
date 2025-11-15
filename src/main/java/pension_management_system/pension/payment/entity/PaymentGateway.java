package pension_management_system.pension.payment.entity;

/**
 * PaymentGateway Enum - Supported payment gateways
 *
 * Purpose: Defines which payment processors the system integrates with
 *
 * What is a Payment Gateway?
 * - Third-party service that processes online payments
 * - Handles credit cards, bank transfers, mobile money
 * - Provides APIs for payment initiation and verification
 * - Manages security, compliance, and fraud detection
 *
 * Why use payment gateways?
 * - Security: PCI-DSS compliant payment processing
 * - Convenience: Don't build payment infrastructure
 * - Multiple payment methods: Cards, banks, wallets
 * - Trust: Established brands users recognize
 *
 * Nigerian Payment Gateways:
 * - Paystack: Popular, developer-friendly, low fees
 * - Flutterwave: Pan-African, multiple currencies
 * - Others: Interswitch, Remita, etc.
 *
 * How it works in our system:
 * 1. User makes contribution
 * 2. We redirect to payment gateway (Paystack or Flutterwave)
 * 3. User completes payment
 * 4. Gateway sends webhook notification
 * 5. We verify and update contribution status
 */
public enum PaymentGateway {

    /**
     * PAYSTACK - Nigerian payment gateway
     *
     * Website: https://paystack.com
     * Documentation: https://paystack.com/docs
     *
     * Features:
     * - Credit/Debit cards (Visa, Mastercard, Verve)
     * - Bank account payments
     * - USSD payments
     * - QR code payments
     * - Subscriptions and recurring payments
     * - Split payments
     *
     * Pricing (as of 2025):
     * - Local cards: 1.5% + ₦100 cap
     * - International cards: 3.9%
     * - Bank transfers: ₦50 flat
     *
     * Integration:
     * - REST API with JSON
     * - Webhook notifications
     * - Test mode with test keys
     * - Production mode with live keys
     *
     * Why choose Paystack?
     * - Very popular in Nigeria
     * - Excellent documentation
     * - Developer-friendly API
     * - Good customer support
     * - Lower fees for local transactions
     *
     * Example flow:
     * 1. Initialize transaction: POST /transaction/initialize
     * 2. Get authorization URL
     * 3. Redirect user to Paystack
     * 4. User pays
     * 5. Paystack redirects back with reference
     * 6. Verify transaction: GET /transaction/verify/:reference
     * 7. Receive webhook confirmation
     */
    PAYSTACK,

    /**
     * FLUTTERWAVE - Pan-African payment gateway
     *
     * Website: https://flutterwave.com
     * Documentation: https://developer.flutterwave.com
     *
     * Features:
     * - Multiple African countries (Nigeria, Ghana, Kenya, etc.)
     * - Multiple currencies (NGN, USD, GHS, KES, etc.)
     * - Cards, bank transfers, mobile money
     * - M-Pesa integration (Kenya)
     * - Ghana Mobile Money
     * - Payment links
     * - Subscriptions
     *
     * Pricing (as of 2025):
     * - Nigerian local cards: 1.4%
     * - International cards: 3.8%
     * - Bank transfers: ₦10 - ₦25
     * - Mobile money: 1.4%
     *
     * Integration:
     * - REST API with JSON
     * - Inline payment modal
     * - Webhook notifications
     * - Test mode and live mode
     *
     * Why choose Flutterwave?
     * - Multi-country support
     * - Multiple currencies
     * - Mobile money integration
     * - Good for pan-African business
     * - Competitive fees
     *
     * Example flow:
     * 1. Initialize payment: POST /payments
     * 2. Get payment link or use inline modal
     * 3. User completes payment
     * 4. Verify transaction: GET /transactions/:id/verify
     * 5. Receive webhook notification
     */
    FLUTTERWAVE,

    /**
     * MANUAL - Manual/offline payment
     *
     * For payments processed outside the system:
     * - Bank deposits
     * - Cash payments at office
     * - Cheque payments
     * - Direct bank transfers (not via gateway)
     *
     * Process:
     * 1. Member makes manual payment
     * 2. Admin verifies payment receipt
     * 3. Admin manually marks contribution as paid
     * 4. System records payment with MANUAL gateway
     *
     * Use cases:
     * - Fallback when gateways are down
     * - Large corporate payments via direct transfer
     * - Legacy payment methods
     * - Special arrangements
     *
     * Limitations:
     * - No automatic verification
     * - Manual reconciliation required
     * - Higher risk of errors
     * - Slower processing
     */
    MANUAL
}
