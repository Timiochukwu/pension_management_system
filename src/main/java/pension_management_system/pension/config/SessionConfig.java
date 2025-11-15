package pension_management_system.pension.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

/**
 * SessionConfig - Redis-based distributed session management
 *
 * Purpose: Enable horizontal scaling with shared sessions across multiple servers
 *
 * Benefits:
 * - Sessions stored in Redis (persistent, fast)
 * - Load balancer can route to any server
 * - Session survives server restart
 * - Perfect for microservices/cloud deployments
 *
 * Session timeout: 30 minutes
 *
 * Production Benefits:
 * - Enables zero-downtime deployments
 * - Auto-failover (if one server dies, session persists)
 * - Horizontal scaling (add more servers without session loss)
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 30 minutes
public class SessionConfig {

    /**
     * Use X-Auth-Token header for session ID
     * Better for APIs than cookies
     */
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }
}
