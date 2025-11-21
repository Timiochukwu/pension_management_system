package pension_management_system.pension.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

/**
 * PaginationConfig - Configure pagination limits
 *
 * Prevents excessive page sizes that could cause performance issues
 */
@Configuration
public class PaginationConfig {

    /**
     * Maximum allowed page size
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Default page size when not specified
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer paginationCustomizer() {
        return resolver -> {
            resolver.setMaxPageSize(MAX_PAGE_SIZE);
            resolver.setFallbackPageable(org.springframework.data.domain.PageRequest.of(0, DEFAULT_PAGE_SIZE));
        };
    }
}
