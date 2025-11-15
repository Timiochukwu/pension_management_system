package pension_management_system.pension.monitoring.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MonitoringConfig - Monitoring and metrics configuration
 *
 * Purpose: Configure monitoring infrastructure
 *
 * Features:
 * - Enable @Timed annotation for method timing
 * - Custom metrics
 * - Health indicators
 * - Scheduled metric updates
 */
@Configuration
@EnableScheduling
public class MonitoringConfig {

    /**
     * Enable @Timed annotation on methods
     *
     * Usage:
     * @Timed(value = "payment.processing", description = "Payment processing time")
     * public void processPayment() { ... }
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
