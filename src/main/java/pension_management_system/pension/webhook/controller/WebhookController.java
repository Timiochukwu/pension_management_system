package pension_management_system.pension.webhook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.webhook.dto.WebhookRequest;
import pension_management_system.pension.webhook.dto.WebhookResponse;
import pension_management_system.pension.webhook.service.WebhookService;

import java.util.List;

/**
 * WebhookController - Webhook management API
 *
 * Endpoints:
 * - Register webhooks
 * - List webhooks
 * - Delete webhooks
 * - View delivery logs
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Enterprise webhook management for real-time integrations")
public class WebhookController {

    private final WebhookService webhookService;

    @Operation(summary = "Register webhook", description = "Register a new webhook endpoint for events")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WebhookResponse> registerWebhook(@Valid @RequestBody WebhookRequest request) {
        WebhookResponse response = webhookService.registerWebhook(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all webhooks", description = "List all registered webhooks")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<WebhookResponse>> getAllWebhooks() {
        List<WebhookResponse> webhooks = webhookService.getAllWebhooks();
        return ResponseEntity.ok(webhooks);
    }

    @Operation(summary = "Delete webhook", description = "Delete a webhook registration")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long id) {
        webhookService.deleteWebhook(id);
        return ResponseEntity.noContent().build();
    }
}
