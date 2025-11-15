package pension_management_system.pension.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Filter to validate JWT tokens on each request
 *
 * Purpose: Intercept HTTP requests and validate JWT bearer tokens
 *
 * What is a Servlet Filter?
 * - Intercepts requests BEFORE reaching controller
 * - Can modify request/response
 * - Can reject requests (authentication, authorization)
 * - Part of Java Servlet specification
 *
 * Filter Chain:
 * Client Request
 *     ↓
 * [JwtAuthenticationFilter] ← We are here
 *     ↓
 * [Other Security Filters]
 *     ↓
 * [Controller]
 *     ↓
 * Response
 *
 * How it works:
 * 1. Extract JWT from Authorization header
 * 2. Validate token using JwtUtil
 * 3. Load user from database
 * 4. Set authentication in SecurityContext
 * 5. Allow request to proceed
 *
 * OncePerRequestFilter:
 * - Ensures filter executes only once per request
 * - Prevents duplicate processing
 * - Spring's recommended approach
 *
 * Authentication Flow:
 *
 * Request with Token              No Token
 *        ↓                            ↓
 *    Extract JWT                  Skip Filter
 *        ↓                            ↓
 *    Validate Token              Continue
 *        ↓
 *    Valid?
 *     ↙    ↘
 *   Yes      No
 *    ↓        ↓
 *  Allow    Deny
 *
 * @Component - Spring manages instance
 * @RequiredArgsConstructor - Constructor injection
 * @Slf4j - Logging support
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * DEPENDENCIES
     */
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * DO FILTER INTERNAL
     *
     * Main filter logic - executes on each request
     *
     * Process:
     * 1. Extract Authorization header
     * 2. Check if it's a Bearer token
     * 3. Extract username from JWT
     * 4. Validate token
     * 5. Authenticate user
     * 6. Continue filter chain
     *
     * Security checks:
     * - Token present?
     * - Token valid format?
     * - User exists?
     * - Token not expired?
     * - Token belongs to user?
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain to continue
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // STEP 1: EXTRACT AUTHORIZATION HEADER
        // Format: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        final String authHeader = request.getHeader("Authorization");

        // STEP 2: CHECK IF HEADER EXISTS AND STARTS WITH "Bearer "
        // If no token, continue without authentication (public endpoints)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found in request headers");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // STEP 3: EXTRACT TOKEN
            // Remove "Bearer " prefix (7 characters)
            final String jwt = authHeader.substring(7);

            // STEP 4: EXTRACT USERNAME FROM TOKEN
            // Token contains username in "sub" claim
            final String userEmail = jwtUtil.extractUsername(jwt);

            log.debug("JWT token found for user: {}", userEmail);

            // STEP 5: CHECK IF USER ALREADY AUTHENTICATED
            // SecurityContextHolder stores authentication for current request
            // If already authenticated, skip validation
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // STEP 6: LOAD USER DETAILS FROM DATABASE
                // UserDetailsService retrieves user info
                // Typically loads from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // STEP 7: VALIDATE TOKEN
                // Check token signature and expiration
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    log.debug("JWT token validated successfully for user: {}", userEmail);

                    // STEP 8: CREATE AUTHENTICATION TOKEN
                    // This represents authenticated user
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials (null after authentication)
                            userDetails.getAuthorities() // User roles/permissions
                    );

                    // STEP 9: ADD REQUEST DETAILS
                    // Store IP address, session ID, etc.
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // STEP 10: SET AUTHENTICATION IN SECURITY CONTEXT
                    // This marks user as authenticated for this request
                    // Spring Security uses this for authorization
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authentication set in security context for user: {}", userEmail);
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                }
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Continue without authentication
            // Controller will handle unauthorized access
        }

        // STEP 11: CONTINUE FILTER CHAIN
        // Pass request to next filter or controller
        filterChain.doFilter(request, response);
    }
}

/**
 * HOW IT INTEGRATES WITH SPRING SECURITY
 *
 * SecurityConfig.java:
 * ```java
 * @Configuration
 * @EnableWebSecurity
 * public class SecurityConfig {
 *
 *     @Autowired
 *     private JwtAuthenticationFilter jwtAuthFilter;
 *
 *     @Bean
 *     public SecurityFilterChain securityFilterChain(HttpSecurity http) {
 *         http
 *             .csrf().disable()
 *             .authorizeHttpRequests(auth -> auth
 *                 .requestMatchers("/api/auth/**").permitAll() // Public
 *                 .anyRequest().authenticated() // Protected
 *             )
 *             .sessionManagement()
 *                 .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
 *             .and()
 *             .addFilterBefore(
 *                 jwtAuthFilter,
 *                 UsernamePasswordAuthenticationFilter.class
 *             );
 *
 *         return http.build();
 *     }
 * }
 * ```
 *
 * Key points:
 * - CSRF disabled (not needed for stateless JWT)
 * - Session stateless (no server-side sessions)
 * - JWT filter runs before authentication filter
 * - Public endpoints bypass authentication
 *
 * REQUEST FLOW EXAMPLE
 *
 * 1. Login (get token):
 * ```
 * POST /api/auth/login
 * Body: {"username": "john@example.com", "password": "pass123"}
 *
 * Response: {"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
 * ```
 *
 * 2. Use token for protected endpoint:
 * ```
 * GET /api/members
 * Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 *
 * Flow:
 * 1. JwtAuthenticationFilter extracts token
 * 2. Validates token
 * 3. Sets authentication in SecurityContext
 * 4. Request reaches MemberController
 * 5. Controller sees authenticated user
 * 6. Returns member data
 * ```
 *
 * 3. Request without token (public endpoint):
 * ```
 * POST /api/auth/register
 * Body: {...}
 *
 * Flow:
 * 1. JwtAuthenticationFilter finds no token
 * 2. Skips authentication
 * 3. Request reaches controller
 * 4. Allowed because /api/auth/** is public
 * ```
 *
 * 4. Request without token (protected endpoint):
 * ```
 * GET /api/members
 * Headers: (no Authorization header)
 *
 * Flow:
 * 1. JwtAuthenticationFilter finds no token
 * 2. Skips authentication
 * 3. Spring Security sees no authentication
 * 4. Returns 401 Unauthorized
 * ```
 *
 * ACCESSING AUTHENTICATED USER IN CONTROLLER
 *
 * ```java
 * @GetMapping("/profile")
 * public ResponseEntity<UserProfile> getProfile() {
 *     // Get authenticated user
 *     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *     UserDetails userDetails = (UserDetails) auth.getPrincipal();
 *     String email = userDetails.getUsername();
 *
 *     // Or use @AuthenticationPrincipal
 * }
 *
 * @GetMapping("/profile")
 * public ResponseEntity<UserProfile> getProfile(
 *         @AuthenticationPrincipal UserDetails userDetails) {
 *     String email = userDetails.getUsername();
 *     // ...
 * }
 * ```
 *
 * ERROR HANDLING
 *
 * Common errors:
 *
 * 1. Token expired:
 *    - jwtUtil.validateToken() returns false
 *    - User not authenticated
 *    - Returns 401
 *    - Client should refresh token or re-login
 *
 * 2. Invalid token format:
 *    - Catches exception
 *    - Logs error
 *    - Continues without authentication
 *
 * 3. User not found:
 *    - userDetailsService.loadUserByUsername() throws exception
 *    - Catches exception
 *    - Continues without authentication
 *
 * 4. Malformed JWT:
 *    - jwtUtil.extractUsername() throws JwtException
 *    - Catches exception
 *    - Logs error
 *    - Continues without authentication
 *
 * SECURITY CONSIDERATIONS
 *
 * 1. Token Theft:
 *    - If JWT stolen, attacker can impersonate user
 *    - Mitigation: Short expiration time
 *    - Use refresh tokens
 *    - Implement token blacklist
 *
 * 2. XSS (Cross-Site Scripting):
 *    - Attacker steals token from localStorage
 *    - Mitigation: Use HttpOnly cookies
 *    - Sanitize user input
 *    - Content Security Policy
 *
 * 3. Man-in-the-Middle:
 *    - Attacker intercepts token
 *    - Mitigation: HTTPS only
 *    - HSTS headers
 *
 * 4. Token Expiration:
 *    - Balance security vs user experience
 *    - Sensitive apps: 15 minutes
 *    - Normal apps: 1-24 hours
 *    - Use sliding expiration
 *
 * TESTING THE FILTER
 *
 * ```java
 * @WebMvcTest
 * @Import(JwtAuthenticationFilter.class)
 * class JwtAuthenticationFilterTest {
 *
 *     @Autowired
 *     private MockMvc mockMvc;
 *
 *     @MockBean
 *     private JwtUtil jwtUtil;
 *
 *     @MockBean
 *     private UserDetailsService userDetailsService;
 *
 *     @Test
 *     void shouldAuthenticateWithValidToken() throws Exception {
 *         // Arrange
 *         String token = "valid.jwt.token";
 *         when(jwtUtil.extractUsername(token)).thenReturn("user@example.com");
 *         when(jwtUtil.validateToken(any(), any())).thenReturn(true);
 *         when(userDetailsService.loadUserByUsername(any()))
 *             .thenReturn(new User("user@example.com", "pass", List.of()));
 *
 *         // Act & Assert
 *         mockMvc.perform(get("/api/members")
 *                 .header("Authorization", "Bearer " + token))
 *             .andExpect(status().isOk());
 *     }
 *
 *     @Test
 *     void shouldRejectInvalidToken() throws Exception {
 *         mockMvc.perform(get("/api/members")
 *                 .header("Authorization", "Bearer invalid.token"))
 *             .andExpect(status().isUnauthorized());
 *     }
 *
 *     @Test
 *     void shouldRejectMissingToken() throws Exception {
 *         mockMvc.perform(get("/api/members"))
 *             .andExpect(status().isUnauthorized());
 *     }
 * }
 * ```
 */
