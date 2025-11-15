package pension_management_system.pension.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pension_management_system.pension.auth.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Member endpoints - accessible by ADMIN, MANAGER, and MEMBER (own data)
                        .requestMatchers("/api/v1/members/**").hasAnyRole("ADMIN", "MANAGER", "MEMBER")

                        // Contribution endpoints - accessible by ADMIN, MANAGER, MEMBER, and OPERATOR
                        .requestMatchers("/api/v1/contributions/**").hasAnyRole("ADMIN", "MANAGER", "MEMBER", "OPERATOR")

                        // Employer endpoints - accessible by ADMIN and MANAGER
                        .requestMatchers("/api/v1/employers/**").hasAnyRole("ADMIN", "MANAGER")

                        // Benefit endpoints - different access levels
                        .requestMatchers("/api/v1/benefits/calculate/**").hasAnyRole("ADMIN", "MANAGER", "MEMBER")
                        .requestMatchers("/api/v1/benefits/*/approve", "/api/v1/benefits/*/reject", "/api/v1/benefits/*/disburse")
                            .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/benefits/**").hasAnyRole("ADMIN", "MANAGER", "MEMBER", "OPERATOR")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

