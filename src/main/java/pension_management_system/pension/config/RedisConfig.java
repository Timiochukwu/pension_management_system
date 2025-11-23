package pension_management_system.pension.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * RedisConfig - Configuration for Redis caching
 *
 * Purpose: Set up Redis for caching frequently accessed data
 *
 * What is Redis?
 * - In-memory data store (super fast)
 * - Key-value database
 * - Used for caching, sessions, queues
 * - Much faster than database queries
 *
 * Why use caching?
 * - Reduce database load
 * - Faster response times
 * - Better scalability
 * - Cost savings
 *
 * Example performance:
 * - Database query: 50-100ms
 * - Redis cache: 1-5ms (10-50x faster!)
 *
 * What we cache:
 * - Dashboard statistics
 * - Analytics data
 * - Top employers list
 * - Report metadata
 *
 * Configuration required (application.properties):
 * spring.data.redis.host=localhost
 * spring.data.redis.port=6379
 * spring.data.redis.password=your_password
 * spring.cache.type=redis
 * spring.cache.redis.time-to-live=600000  # 10 minutes in ms
 *
 * Annotations:
 * @Configuration - Spring configuration class
 * @EnableCaching - Enable Spring's caching support
 * @ConditionalOnProperty - Only load this config when Redis is enabled
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class RedisConfig {

    /**
     * CACHE MANAGER
     *
     * Central manager for all caches
     * Spring uses this to create and manage caches
     *
     * Configuration:
     * - TTL: Time To Live (how long data stays in cache)
     * - Serialization: How to convert objects to bytes for Redis
     * - Key prefixes: Organize cache keys
     *
     * @param connectionFactory Redis connection factory
     * @return Configured cache manager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // STEP 1: Create ObjectMapper for JSON serialization
        // Handles Java 8 date/time types
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Enable type information to prevent ClassCastException (LinkedHashMap issue)
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

        // STEP 2: Configure how to serialize values
        // GenericJackson2JsonRedisSerializer: Objects → JSON → Redis
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // STEP 3: Configure cache settings
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // Cache entries expire after 10 minutes
                .entryTtl(Duration.ofMinutes(10))
                // Store keys as strings
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                // Store values as JSON
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                )
                // Don't cache null values (saves space)
                .disableCachingNullValues();

        // STEP 4: Create and return cache manager
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * REDIS TEMPLATE
     *
     * Lower-level Redis operations
     * Use when @Cacheable annotations aren't enough
     *
     * Provides:
     * - opsForValue(): String operations
     * - opsForHash(): Hash operations
     * - opsForList(): List operations
     * - opsForSet(): Set operations
     * - opsForZSet(): Sorted set operations
     *
     * Example usage:
     * redisTemplate.opsForValue().set("key", "value", 10, TimeUnit.MINUTES);
     * String value = redisTemplate.opsForValue().get("key");
     *
     * @param connectionFactory Redis connection factory
     * @return Configured Redis template
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Enable type information to prevent ClassCastException (LinkedHashMap issue)
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Key serializer (always string)
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value serializer (JSON)
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}

/**
 * USAGE EXAMPLES
 *
 * 1. Using @Cacheable annotation:
 *
 * @Service
 * public class AnalyticsService {
 *
 *     @Cacheable(value = "dashboard-stats", key = "#root.methodName")
 *     public DashboardStatistics getDashboardStatistics() {
 *         // Expensive database queries here
 *         // Result will be cached for 10 minutes
 *         return statistics;
 *     }
 * }
 *
 * 2. Using @CacheEvict annotation (clear cache):
 *
 * @CacheEvict(value = "dashboard-stats", allEntries = true)
 * public void updateStatistics() {
 *     // When data changes, clear the cache
 * }
 *
 * 3. Using RedisTemplate directly:
 *
 * @Autowired
 * private RedisTemplate<String, Object> redisTemplate;
 *
 * public void saveToCache(String key, Object value) {
 *     redisTemplate.opsForValue().set(key, value, 10, TimeUnit.MINUTES);
 * }
 *
 * public Object getFromCache(String key) {
 *     return redisTemplate.opsForValue().get(key);
 * }
 *
 * CACHE STRATEGIES:
 *
 * 1. Cache-Aside (Most common):
 *    - Check cache first
 *    - If miss, query database
 *    - Store result in cache
 *    - Return result
 *
 * 2. Write-Through:
 *    - Write to cache and database simultaneously
 *    - Ensures consistency
 *
 * 3. Write-Behind:
 *    - Write to cache immediately
 *    - Write to database asynchronously
 *    - Better performance but risk of data loss
 *
 * BEST PRACTICES:
 *
 * 1. Cache frequently accessed data
 * 2. Use appropriate TTL (don't cache forever)
 * 3. Handle cache failures gracefully
 * 4. Monitor cache hit rates
 * 5. Clear cache when data changes
 * 6. Don't cache user-specific data (privacy)
 * 7. Use meaningful cache keys
 *
 * MONITORING:
 *
 * # Redis CLI commands to monitor cache
 * redis-cli
 * > KEYS *                  # List all keys
 * > GET key_name            # Get value
 * > TTL key_name            # Check time to live
 * > FLUSHALL                # Clear all cache (careful!)
 * > INFO stats              # View statistics
 */
