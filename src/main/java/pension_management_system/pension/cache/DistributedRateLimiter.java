package pension_management_system.pension.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * DistributedRateLimiter - Redis-based rate limiting across multiple servers
 *
 * Purpose: Enforce rate limits globally (not per-server)
 *
 * Why Redis?
 * - Single source of truth for all servers
 * - Atomic operations (thread-safe)
 * - Fast (< 1ms per check)
 *
 * Use cases:
 * - API rate limiting (100 req/min per user)
 * - Login attempt limiting (5 attempts/15 minutes)
 * - Payment initiation limiting (10/hour)
 * - OTP request limiting (3/hour)
 *
 * Note: Only enabled when Redis is configured
 */
@Component
@ConditionalOnBean(RedisTemplate.class)
public class DistributedRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    public DistributedRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if action is allowed for user
     *
     * @param key Unique identifier (e.g., "login:user@email.com" or "api:192.168.1.1")
     * @param maxAttempts Maximum allowed attempts
     * @param window Time window duration
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, int maxAttempts, Duration window) {
        String redisKey = "rate_limit:" + key;

        // Get current count
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            currentCount = 1L;
        }

        // Set expiry on first request
        if (currentCount == 1) {
            redisTemplate.expire(redisKey, window.getSeconds(), TimeUnit.SECONDS);
        }

        // Check if within limit
        return currentCount <= maxAttempts;
    }

    /**
     * Record a failed attempt and check if limit exceeded
     *
     * @param key Unique identifier
     * @param maxAttempts Maximum allowed attempts
     * @param lockoutDuration How long to lock out after exceeding
     * @return true if allowed, false if locked out
     */
    public boolean recordAttempt(String key, int maxAttempts, Duration lockoutDuration) {
        return isAllowed(key, maxAttempts, lockoutDuration);
    }

    /**
     * Get remaining attempts
     */
    public int getRemainingAttempts(String key, int maxAttempts) {
        String redisKey = "rate_limit:" + key;
        Integer count = (Integer) redisTemplate.opsForValue().get(redisKey);

        if (count == null) {
            return maxAttempts;
        }

        return Math.max(0, maxAttempts - count);
    }

    /**
     * Reset rate limit for a key (e.g., after successful login)
     */
    public void reset(String key) {
        redisTemplate.delete("rate_limit:" + key);
    }

    /**
     * Get time until rate limit resets
     */
    public long getTimeToReset(String key) {
        String redisKey = "rate_limit:" + key;
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : 0;
    }
}
