package pension_management_system.pension.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for rate limiting using Bucket4j
 *
 * Uses Token Bucket algorithm to limit requests per IP address
 */
@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Rate limit: 100 requests per minute
    private static final int REQUESTS_PER_MINUTE = 100;

    /**
     * Get or create a bucket for the given key (usually client IP)
     */
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::createNewBucket);
    }

    /**
     * Create a new rate limiting bucket
     */
    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                REQUESTS_PER_MINUTE,
                Refill.greedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
        );
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}
