package pension_management_system.pension.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
<<<<<<< HEAD
=======
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import pension_management_system.pension.auth.filter.JwtAuthenticationFilter;
>>>>>>> origin/claude/fix-duplicate-user-entity-01MrLcHPHC6E6XLcj5wqAX4V

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // disable CSRF for testing
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // allow all requests
                )
                .httpBasic().disable()  // disable basic auth
                .formLogin().disable(); // disable login form
        return http.build();
    }

}

