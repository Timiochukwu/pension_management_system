package pension_management_system.pension.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS CONFIGURATION
 *
 * Purpose: Allow frontend to make requests to backend API
 *
 * This configuration enables:
 * - React frontend (http://localhost:5173) to call backend APIs
 * - Production frontend to call backend APIs
 * - All HTTP methods (GET, POST, PUT, DELETE)
 * - JWT token in Authorization header
 * - Credentials/cookies to be sent
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow frontend origins
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5173",           // Vite dev server
            "http://localhost:3000",           // Alternative React dev server
            "http://localhost:4173",           // Vite preview
            "https://*.vercel.app",            // Vercel deployments
            "https://*.netlify.app",           // Netlify deployments
            "https://yourdomain.com"           // Production domain (update this)
        ));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control"
        ));

        // Expose headers to frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition"
        ));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        // Apply CORS configuration to all API endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
