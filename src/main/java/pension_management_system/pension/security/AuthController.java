package pension_management_system.pension.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userDetails.UserDetails;
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
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate JWT
            String token = jwtUtil.generateToken(userDetails);

            log.info("Login successful for user: {}", request.getUsername());

            // Create user info response
            UserInfo userInfo = new UserInfo(
                    1L,  // TODO: Get actual user ID from database
                    "Admin",  // TODO: Get actual first name from database
                    "User",   // TODO: Get actual last name from database
                    userDetails.getUsername(),
                    "ADMIN"  // TODO: Get actual role from database
            );

            // Return token with user info
            return ResponseEntity.ok(new AuthResponse(
                    token,
                    "Bearer",
                    userDetails.getUsername(),
                    userInfo
            ));

        } catch (Exception e) {
            log.error("Login failed for user {}: {}", request.getUsername(), e.getMessage());
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
        String token = request.getToken();

        try {
            String username = jwtUtil.extractUsername(token);

            if (username != null && !jwtUtil.isTokenExpired(token)) {
                // Token still valid, generate new one
                // Note: Load user from database in real implementation
                org.springframework.security.core.userdetails.User user =
                        new org.springframework.security.core.userdetails.User(
                                username, "", java.util.List.of());

                String newToken = jwtUtil.generateToken(user);

                // Create user info for refresh response
                UserInfo userInfo = new UserInfo(
                        1L,  // TODO: Get actual user ID from database
                        "Admin",  // TODO: Get actual first name from database
                        "User",   // TODO: Get actual last name from database
                        username,
                        "ADMIN"  // TODO: Get actual role from database
                );

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
