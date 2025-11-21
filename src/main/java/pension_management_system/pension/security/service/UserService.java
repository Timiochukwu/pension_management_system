package pension_management_system.pension.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.security.entity.User;
import pension_management_system.pension.security.repository.UserRepository;

/**
 * USER SERVICE
 *
 * Purpose: Handle user management operations
 *
 * Features:
 * - User registration with validation
 * - Password encryption
 * - Duplicate check (username/email)
 * - User updates
 */
@Service("securityUserService")
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * REGISTER NEW USER
     *
     * Creates a new user account with encrypted password
     *
     * @param user User entity to create
     * @return Created user (without password)
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public User registerUser(User user) {
        log.info("Registering new user: {}", user.getUsername());

        // Validate username is unique
        if (userRepository.existsByUsername(user.getUsername())) {
            log.error("Username already exists: {}", user.getUsername());
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        // Validate email is unique
        if (userRepository.existsByEmail(user.getEmail())) {
            log.error("Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Encrypt password
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        // Set defaults if not provided
        if (user.getRole() == null) {
            user.setRole(User.UserRole.MEMBER); // Default role
        }
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        if (user.getAccountNonExpired() == null) {
            user.setAccountNonExpired(true);
        }
        if (user.getAccountNonLocked() == null) {
            user.setAccountNonLocked(true);
        }
        if (user.getCredentialsNonExpired() == null) {
            user.setCredentialsNonExpired(true);
        }

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        // Note: Don't clear password here - savedUser is a managed entity
        // Clearing it would trigger an UPDATE that sets password=null
        // The controller maps to UserResponse DTO which excludes password
        return savedUser;
    }

    /**
     * FIND USER BY ID
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    /**
     * FIND USER BY USERNAME
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    /**
     * FIND USER BY EMAIL
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }
}
