package pension_management_system.pension.monitoring.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * PaymentGatewayHealthIndicator - Health check for payment gateways
 *
 * Purpose: Check if payment gateways (Paystack, Flutterwave) are accessible
 *
 * Health status:
 * - UP: At least one gateway is accessible
 * - DOWN: No gateways are accessible
 */
@Component
@RequiredArgsConstructor
public class PaymentGatewayHealthIndicator implements HealthIndicator {

    private final WebClient.Builder webClientBuilder;

    @Override
    public Health health() {
        boolean paystackUp = checkPaystack();
        boolean flutterwaveUp = checkFlutterwave();

        if (paystackUp || flutterwaveUp) {
            return Health.up()
                    .withDetail("paystack", paystackUp ? "UP" : "DOWN")
                    .withDetail("flutterwave", flutterwaveUp ? "UP" : "DOWN")
                    .build();
        } else {
            return Health.down()
                    .withDetail("paystack", "DOWN")
                    .withDetail("flutterwave", "DOWN")
                    .withDetail("error", "All payment gateways are unavailable")
                    .build();
        }
    }

    private boolean checkPaystack() {
        try {
            WebClient webClient = webClientBuilder.build();

            webClient.get()
                    .uri("https://api.paystack.co")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkFlutterwave() {
        try {
            WebClient webClient = webClientBuilder.build();

            webClient.get()
                    .uri("https://api.flutterwave.com")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
