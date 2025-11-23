package pension_management_system.pension.monitoring.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * RedisHealthIndicator - Custom health check for Redis
 *
 * Purpose: Check if Redis is accessible and responsive
 *
 * Health status:
 * - UP: Redis is accessible
 * - DOWN: Redis is not accessible
 *
 * Note: Only enabled when Redis is configured
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RedisTemplate.class)
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            // Test Redis connection by setting and getting a key
            String testKey = "health:check";
            String testValue = "ok";

            redisTemplate.opsForValue().set(testKey, testValue);
            Object result = redisTemplate.opsForValue().get(testKey);

            if (testValue.equals(result)) {
                redisTemplate.delete(testKey);

                return Health.up()
                        .withDetail("redis", "Connected")
                        .withDetail("status", "Healthy")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Redis read/write test failed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
