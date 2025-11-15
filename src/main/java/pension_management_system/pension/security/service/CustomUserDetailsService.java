package pension_management_system.pension.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.security.entity.User;
import pension_management_system.pension.security.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;

/**
 * CUSTOM USER DETAILS SERVICE
 *
 * Purpose: Load user from database for Spring Security authentication
 *
 * Features:
 * - Loads user by username or email
 * - Converts User entity to Spring Security UserDetails
 * - Handles account status (enabled, locked, expired)
 * - Maps role to granted authority
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * LOAD USER BY USERNAME
     *
     * Called by Spring Security during authentication
     * Supports both username and email for login
     *
     * @param usernameOrEmail Username or email
     * @return UserDetails for authentication
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by username or email: {}", usernameOrEmail);

        // Find user by email or username
        User user = userRepository.findByEmailOrUsername(usernameOrEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", usernameOrEmail);
                    return new UsernameNotFoundException("User not found: " + usernameOrEmail);
                });

        log.debug("User found: {} with role: {}", user.getUsername(), user.getRole());

        // Convert to Spring Security UserDetails
        return new CustomUserDetails(user);
    }

    /**
     * CUSTOM USER DETAILS
     *
     * Wrapper class that implements Spring Security UserDetails
     * Provides user information for authentication and authorization
     */
    public static class CustomUserDetails implements UserDetails {
        private final User user;

        public CustomUserDetails(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Convert role to granted authority with "ROLE_" prefix
            return Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return user.getAccountNonExpired();
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getAccountNonLocked();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return user.getCredentialsNonExpired();
        }

        @Override
        public boolean isEnabled() {
            return user.getEnabled();
        }

        /**
         * Get the actual User entity
         * Useful for accessing additional user properties
         */
        public User getUser() {
            return user;
        }
    }
}
