package pension_management_system.pension.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Cache Configuration - Enables Spring Cache support
 * Required for @Cacheable, @CachePut, @CacheEvict annotations to work
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // Using simple in-memory cache (configured in application.properties)
    // For production, consider using Redis or Ehcache
}
