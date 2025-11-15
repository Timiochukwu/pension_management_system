package pension_management_system.pension.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RateLimitConfig - API rate limiting configuration
 *
 * Purpose: Prevent API abuse and DDoS attacks
 *
 * What is Rate Limiting?
 * - Limit number of requests per time period
 * - Prevents single user from overwhelming system
 * - Fair usage for all users
 *
 * Implementation: Token Bucket algorithm
 * - Bucket has capacity (e.g., 100 tokens)
 * - Each request consumes 1 token
 * - Tokens refill over time
 * - If bucket empty, request rejected
 *
 * Example:
 * - 100 requests per minute
 * - Bucket capacity: 100
 * - Refill: 100 tokens per minute
 *
 * Use with:
 * @RateLimit annotation on controllers
 *
 * @Configuration - Spring configuration
 */
@Configuration
public class RateLimitConfig {

    // Store buckets per user (IP or user ID)
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * GET BUCKET FOR USER
     *
     * Returns rate limit bucket for user
     * Creates new bucket if doesn't exist
     *
     * @param key User identifier (IP or email)
     * @return Token bucket for rate limiting
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * CREATE NEW BUCKET
     *
     * Token Bucket configuration:
     * - Capacity: 100 tokens
     * - Refill: 100 tokens per minute
     *
     * Allows burst of 100 requests,
     * then sustained 100 req/min
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
                100, // capacity
                Refill.intervally(100, Duration.ofMinutes(1)) // refill rate
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

/**
 * USAGE IN FILTER/INTERCEPTOR:
 *
 * @Component
 * public class RateLimitFilter extends OncePerRequestFilter {
 *
 *     @Autowired
 *     private RateLimitConfig rateLimitConfig;
 *
 *     @Override
 *     protected void doFilterInternal(HttpServletRequest request,
 *                                     HttpServletResponse response,
 *                                     FilterChain filterChain) {
 *
 *         String key = getClientIP(request);
 *         Bucket bucket = rateLimitConfig.resolveBucket(key);
 *
 *         if (bucket.tryConsume(1)) {
 *             // Allow request
 *             filterChain.doFilter(request, response);
 *         } else {
 *             // Rate limit exceeded
 *             response.setStatus(429); // Too Many Requests
 *             response.getWriter().write("Rate limit exceeded");
 *         }
 *     }
 * }
 *
 * CONFIGURATION IN application.properties:
 *
 * # Rate limiting
 * rate-limit.enabled=true
 * rate-limit.requests-per-minute=100
 * rate-limit.burst-capacity=120
 */
