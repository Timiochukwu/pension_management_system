package pension_management_system.pension.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import pension_management_system.pension.security.entity.User;
import pension_management_system.pension.security.service.CustomUserDetailsService;
import pension_management_system.pension.security.service.UserService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * AuthController - Authentication endpoints
 *
 * Purpose: Handle login and registration
 *
 * Endpoints:
 * - POST /api/auth/login - Authenticate user, return JWT
 * - POST /api/auth/register - Register new user
 *
 * These endpoints are PUBLIC (no authentication required)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final pension_management_system.pension.security.repository.UserRepository userRepository;

    /**
     * LOGIN ENDPOINT
     *
     * Authenticates user and returns JWT token
     *
     * Request:
     * POST /api/auth/login
     * {
     *   "username": "user@example.com",
     *   "password": "password123"
     * }
     *
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "username": "user@example.com"
     * }
     *
     * @param request Login credentials
     * @return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.username());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            // Get user details from authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Extract actual User entity from CustomUserDetails
            User user = null;
            if (userDetails instanceof CustomUserDetailsService.CustomUserDetails) {
                user = ((CustomUserDetailsService.CustomUserDetails) userDetails).getUser();
                // Update last login timestamp
                user.updateLastLogin();
            }

            // Generate JWT
            String token = jwtUtil.generateToken(userDetails);

            log.info("Login successful for user: {}", request.username());

            // Create user info response from actual database user
            UserInfo userInfo = (user != null) ? new UserInfo(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole().name()
            ) : new UserInfo(
                    1L,
                    "Unknown",
                    "User",
                    userDetails.getUsername(),
                    "MEMBER"
            );

            // Return token with user info
            return ResponseEntity.ok(new AuthResponse(
                    token,
                    "Bearer",
                    userDetails.getUsername(),
                    userInfo
            ));

        } catch (Exception e) {
            log.error("Login failed for user {}: {}", request.username(), e.getMessage());
            throw e;
        }
    }

    /**
     * REGISTER ENDPOINT
     *
     * Creates a new user account
     *
     * Request:
     * POST /api/auth/register
     * {
     *   "username": "john.doe",
     *   "email": "john.doe@example.com",
     *   "password": "password123",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "phoneNumber": "1234567890"
     * }
     *
     * Response:
     * {
     *   "id": 1,
     *   "username": "john.doe",
     *   "email": "john.doe@example.com",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "role": "MEMBER"
     * }
     *
     * @param request Registration details
     * @return Created user (without password)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.username());

        try {
            // Build user entity from request
            User user = User.builder()
                    .username(request.username())
                    .email(request.email())
                    .password(request.password())
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .phoneNumber(request.phoneNumber())
                    .role(User.UserRole.MEMBER) // Default role for registration
                    .build();

            // Register user (password will be encrypted by service)
            User savedUser = userService.registerUser(user);

            log.info("User registered successfully: {}", savedUser.getUsername());

            // Return user response (password already cleared by service)
            return ResponseEntity.ok(new UserResponse(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getRole().name(),
                    savedUser.getPhoneNumber()
            ));

        } catch (IllegalArgumentException e) {
            log.error("Registration failed for user {}: {}", request.username(), e.getMessage());
            throw e;
        }
    }

    /**
     * REFRESH TOKEN ENDPOINT
     *
     * Get new token using existing valid token
     *
     * @param token Current JWT token
     * @return New JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        String token = request.token();

        try {
            String username = jwtUtil.extractUsername(token);

            if (username != null && !jwtUtil.isTokenExpired(token)) {
                // Load actual user from database
                User dbUser = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username));

                // Generate new token
                org.springframework.security.core.userdetails.User user =
                        new org.springframework.security.core.userdetails.User(
                                username, "", java.util.List.of());

                String newToken = jwtUtil.generateToken(user);

                // Create user info with actual data from database
                UserInfo userInfo = new UserInfo(
                        dbUser.getId(),
                        dbUser.getFirstName(),
                        dbUser.getLastName(),
                        dbUser.getEmail(),
                        dbUser.getRole().name()
                );

                log.info("Token refreshed successfully for user: {}", username);
                return ResponseEntity.ok(new AuthResponse(newToken, "Bearer", username, userInfo));
            }

            return ResponseEntity.status(401).build();

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    // DTOs for requests/responses

    public record LoginRequest(
            @jakarta.validation.constraints.NotBlank String username,
            @jakarta.validation.constraints.NotBlank String password
    ) {}

    public record AuthResponse(
            String token,
            String type,
            String username,
            UserInfo user
    ) {}

    public record UserInfo(
            Long id,
            String firstName,
            String lastName,
            String email,
            String role
    ) {}

    public record RefreshTokenRequest(
            String token
    ) {}

    public record RegisterRequest(
            @jakarta.validation.constraints.NotBlank(message = "Username is required")
            @jakarta.validation.constraints.Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,

            @jakarta.validation.constraints.NotBlank(message = "Email is required")
            @jakarta.validation.constraints.Email(message = "Email must be valid")
            String email,

            @jakarta.validation.constraints.NotBlank(message = "Password is required")
            @jakarta.validation.constraints.Size(min = 6, message = "Password must be at least 6 characters")
            String password,

            @jakarta.validation.constraints.NotBlank(message = "First name is required")
            String firstName,

            @jakarta.validation.constraints.NotBlank(message = "Last name is required")
            String lastName,

            String phoneNumber
    ) {}

    public record UserResponse(
            Long id,
            String username,
            String email,
            String firstName,
            String lastName,
            String role,
            String phoneNumber
    ) {}
}

/**
 * USAGE FROM CLIENT
 *
 * JavaScript example:
 * ```javascript
 * // Login
 * const loginResponse = await fetch('/api/auth/login', {
 *     method: 'POST',
 *     headers: {'Content-Type': 'application/json'},
 *     body: JSON.stringify({
 *         username: 'user@example.com',
 *         password: 'password123'
 *     })
 * });
 *
 * const {token} = await loginResponse.json();
 *
 * // Store token
 * localStorage.setItem('authToken', token);
 *
 * // Use token for API calls
 * const membersResponse = await fetch('/api/members', {
 *     headers: {
 *         'Authorization': `Bearer ${token}`
 *     }
 * });
 * ```
 */
