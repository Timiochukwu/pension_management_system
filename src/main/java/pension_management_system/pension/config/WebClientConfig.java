package pension_management_system.pension.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClientConfig - HTTP client configuration
 *
 * Purpose: Configure WebClient for making HTTP requests
 *
 * Used by:
 * - Webhook delivery
 * - BVN verification
 * - Payment gateway health checks
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
