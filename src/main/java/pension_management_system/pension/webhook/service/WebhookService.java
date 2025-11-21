package pension_management_system.pension.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import pension_management_system.pension.webhook.dto.WebhookRequest;
import pension_management_system.pension.webhook.dto.WebhookResponse;
import pension_management_system.pension.webhook.entity.Webhook;
import pension_management_system.pension.webhook.entity.WebhookDelivery;
import pension_management_system.pension.webhook.repository.WebhookDeliveryRepository;
import pension_management_system.pension.webhook.repository.WebhookRepository;
import pension_management_system.pension.exception.WebhookException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * WebhookService - Webhook management and delivery
 *
 * Purpose: Enterprise webhook system for real-time integrations
 *
 * Features:
 * - Register webhook endpoints
 * - Deliver events with retry logic
 * - HMAC signature for security
 * - Delivery tracking and audit
 *
 * Business Value:
 * - Enables enterprise integrations
 * - Real-time event notifications
 * - Required by major clients
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * Register a new webhook
     */
    public WebhookResponse registerWebhook(WebhookRequest request) {
        String currentUser = getCurrentUsername();

        // Generate secret for HMAC signing
        String secret = generateSecret();

        Webhook webhook = Webhook.builder()
                .url(request.getUrl())
                .secret(secret)
                .events(request.getEvents())
                .description(request.getDescription())
                .createdBy(currentUser)
                .retryCount(request.getRetryCount())
                .timeoutSeconds(request.getTimeoutSeconds())
                .active(true)
                .failureCount(0)
                .build();

        webhook = webhookRepository.save(webhook);
        log.info("Webhook registered: id={}, url={}", webhook.getId(), webhook.getUrl());

        return mapToResponse(webhook, true); // Include secret in response
    }

    /**
     * Get all webhooks
     */
    public List<WebhookResponse> getAllWebhooks() {
        return webhookRepository.findAll().stream()
                .map(w -> mapToResponse(w, false)) // Don't include secret
                .collect(Collectors.toList());
    }

    /**
     * Get active webhooks for specific event
     */
    public List<Webhook> getActiveWebhooksForEvent(String eventType) {
        return webhookRepository.findByActiveTrueAndEventsContaining(eventType);
    }

    /**
     * Delete webhook
     */
    public void deleteWebhook(Long id) {
        webhookRepository.deleteById(id);
        log.info("Webhook deleted: id={}", id);
    }

    /**
     * Trigger webhook for event
     *
     * @param eventType Event type (e.g., "PAYMENT_SUCCESS")
     * @param payload Event data
     */
    @Async
    public void triggerEvent(String eventType, Object payload) {
        log.info("Triggering webhooks for event: {}", eventType);

        List<Webhook> webhooks = getActiveWebhooksForEvent(eventType);

        for (Webhook webhook : webhooks) {
            deliverWebhook(webhook, eventType, payload);
        }
    }

    /**
     * Deliver webhook with retry logic
     */
    private void deliverWebhook(Webhook webhook, String eventType, Object payload) {
        LocalDateTime startTime = LocalDateTime.now();

        try {
            // Convert payload to JSON
            String jsonPayload = objectMapper.writeValueAsString(payload);

            // Generate HMAC signature
            String signature = generateHmacSignature(jsonPayload, webhook.getSecret());

            // Configure retry
            RetryConfig retryConfig = RetryConfig.custom()
                    .maxAttempts(webhook.getRetryCount())
                    .waitDuration(Duration.ofSeconds(5))
                    .build();

            Retry retry = Retry.of("webhook-delivery", retryConfig);

            // Build WebClient
            WebClient webClient = webClientBuilder.build();

            // Deliver with retry
            retry.executeSupplier(() -> {
                try {
                    String response = webClient.post()
                            .uri(webhook.getUrl())
                            .header("X-Webhook-Signature", signature)
                            .header("X-Event-Type", eventType)
                            .header("Content-Type", "application/json")
                            .bodyValue(jsonPayload)
                            .retrieve()
                            .bodyToMono(String.class)
                            .timeout(Duration.ofSeconds(webhook.getTimeoutSeconds()))
                            .block();

                    // Success
                    long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
                    logSuccessfulDelivery(webhook, eventType, jsonPayload, response, duration);

                    return response;
                } catch (Exception e) {
                    log.error("Webhook delivery failed: {}", e.getMessage());
                    throw WebhookException.deliveryFailed(e);
                }
            });

        } catch (Exception e) {
            // Final failure after retries
            long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
            logFailedDelivery(webhook, eventType, e.getMessage(), duration);

            // Increment failure count
            webhook.setFailureCount(webhook.getFailureCount() + 1);

            // Disable webhook after 10 consecutive failures
            if (webhook.getFailureCount() >= 10) {
                webhook.setActive(false);
                log.warn("Webhook disabled due to repeated failures: id={}", webhook.getId());
            }

            webhookRepository.save(webhook);
        }
    }

    /**
     * Generate HMAC-SHA256 signature
     */
    private String generateHmacSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw WebhookException.signatureFailed(e);
        }
    }

    /**
     * Verify HMAC signature
     */
    public boolean verifySignature(String payload, String signature, String secret) {
        String expectedSignature = generateHmacSignature(payload, secret);
        return expectedSignature.equals(signature);
    }

    /**
     * Log successful delivery
     */
    private void logSuccessfulDelivery(
            Webhook webhook,
            String eventType,
            String payload,
            String response,
            long duration
    ) {
        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhook(webhook)
                .eventType(eventType)
                .payload(payload)
                .responseStatus(200)
                .responseBody(response)
                .status(WebhookDelivery.DeliveryStatus.SUCCESS)
                .attemptCount(1)
                .durationMs(duration)
                .build();

        deliveryRepository.save(delivery);

        webhook.setLastTriggeredAt(LocalDateTime.now());
        webhook.setFailureCount(0); // Reset on success
        webhookRepository.save(webhook);

        log.info("Webhook delivered successfully: id={}, event={}, duration={}ms",
                webhook.getId(), eventType, duration);
    }

    /**
     * Log failed delivery
     */
    private void logFailedDelivery(
            Webhook webhook,
            String eventType,
            String errorMessage,
            long duration
    ) {
        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhook(webhook)
                .eventType(eventType)
                .status(WebhookDelivery.DeliveryStatus.FAILED)
                .errorMessage(errorMessage)
                .attemptCount(webhook.getRetryCount())
                .durationMs(duration)
                .build();

        deliveryRepository.save(delivery);

        log.error("Webhook delivery failed: id={}, event={}, error={}",
                webhook.getId(), eventType, errorMessage);
    }

    /**
     * Generate random secret for webhook
     */
    private String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Get current username
     */
    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    /**
     * Map entity to response
     */
    private WebhookResponse mapToResponse(Webhook webhook, boolean includeSecret) {
        return WebhookResponse.builder()
                .id(webhook.getId())
                .url(webhook.getUrl())
                .events(webhook.getEvents())
                .active(webhook.getActive())
                .description(webhook.getDescription())
                .secret(includeSecret ? webhook.getSecret() : null)
                .failureCount(webhook.getFailureCount())
                .lastTriggeredAt(webhook.getLastTriggeredAt())
                .createdAt(webhook.getCreatedAt())
                .build();
    }
}
