package pension_management_system.pension.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtUtil - JSON Web Token utility for authentication
 *
 * Purpose: Generate and validate JWT tokens for API security
 *
 * What is JWT?
 * - JSON Web Token
 * - Compact, URL-safe means of representing claims
 * - Used for stateless authentication
 * - Consists of 3 parts: Header.Payload.Signature
 *
 * JWT Structure:
 * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.     ← Header (algorithm, type)
 * eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ikpva...  ← Payload (claims/data)
 * SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ← Signature (verification)
 *
 * How it works:
 * 1. User logs in with username/password
 * 2. Server validates credentials
 * 3. Server generates JWT with user info
 * 4. Client stores JWT (localStorage/cookie)
 * 5. Client sends JWT in Authorization header for each request
 * 6. Server validates JWT and allows/denies access
 *
 * Authorization Header Format:
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 *
 * Benefits:
 * - Stateless (no session storage needed)
 * - Scalable (works across multiple servers)
 * - Mobile-friendly
 * - Supports microservices architecture
 *
 * Claims in JWT:
 * - sub (subject): User identifier
 * - iat (issued at): Token creation time
 * - exp (expiration): Token expiry time
 * - Custom claims: Any user data you want
 *
 * Security:
 * - Token is signed with secret key
 * - Tampering is detectable
 * - Signature verification ensures authenticity
 * - Expiration prevents old token usage
 *
 * @Component - Spring manages this as singleton
 * @Slf4j - Logging support
 */
@Component
@Slf4j
public class JwtUtil {

    /**
     * CONFIGURATION
     *
     * Secret key for signing JWTs
     * MUST be strong and kept secret!
     *
     * Generate strong secret:
     * openssl rand -base64 64
     *
     * In production:
     * - Use environment variable
     * - Use AWS Secrets Manager / Azure Key Vault
     * - Rotate keys periodically
     * - Never commit to Git!
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token expiration time
     * Default: 24 hours (86400000 ms)
     *
     * Adjust based on security needs:
     * - Sensitive apps: 15 minutes
     * - Normal apps: 1-24 hours
     * - Long-lived: Use refresh tokens
     */
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    /**
     * EXTRACT USERNAME FROM TOKEN
     *
     * JWT payload contains claims (user data)
     * Subject claim typically contains username/email
     *
     * @param token JWT token
     * @return Username extracted from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * EXTRACT EXPIRATION DATE
     *
     * Check when token expires
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * EXTRACT SPECIFIC CLAIM
     *
     * Generic method to extract any claim from token
     *
     * Claims are key-value pairs in JWT payload
     * Standard claims: sub, iat, exp
     * Custom claims: role, permissions, etc.
     *
     * @param token JWT token
     * @param claimsResolver Function to extract specific claim
     * @param <T> Type of claim value
     * @return Claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * EXTRACT ALL CLAIMS
     *
     * Parse and decode JWT token
     * Verify signature
     * Return all claims
     *
     * Process:
     * 1. Parse token using secret key
     * 2. Verify signature (ensures not tampered)
     * 3. Extract payload
     * 4. Return claims
     *
     * @param token JWT token
     * @return All claims from token
     * @throws JwtException if token invalid or expired
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * GET SIGNING KEY
     *
     * Convert secret string to cryptographic key
     * Used for signing and verifying JWTs
     *
     * HMAC-SHA256 algorithm:
     * - Hash-based Message Authentication Code
     * - SHA-256 hashing
     * - Symmetric key (same key for sign and verify)
     *
     * @return Cryptographic key for JWT signing
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * CHECK IF TOKEN EXPIRED
     *
     * Compare expiration date with current time
     *
     * @param token JWT token
     * @return true if token expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * VALIDATE TOKEN
     *
     * Check if token is valid for given user
     *
     * Validation checks:
     * 1. Username in token matches user
     * 2. Token not expired
     *
     * @param token JWT token
     * @param userDetails User details from database
     * @return true if token valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * GENERATE TOKEN
     *
     * Create JWT for authenticated user
     *
     * Process:
     * 1. Create claims (user data)
     * 2. Set subject (username)
     * 3. Set issued at time
     * 4. Set expiration time
     * 5. Sign with secret key
     * 6. Return token string
     *
     * @param userDetails User details (from authentication)
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * GENERATE TOKEN WITH CUSTOM CLAIMS
     *
     * Create JWT with additional custom data
     *
     * Custom claims examples:
     * - Role: {"role": "ADMIN"}
     * - Permissions: {"permissions": ["READ", "WRITE"]}
     * - Member ID: {"memberId": 123}
     *
     * @param extraClaims Custom claims to include
     * @param username User identifier
     * @return JWT token string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * CREATE TOKEN (INTERNAL)
     *
     * Build and sign JWT
     *
     * JWT Builder pattern:
     * - setClaims(): Add custom data
     * - setSubject(): Set username
     * - setIssuedAt(): Token creation time
     * - setExpiration(): Token expiry time
     * - signWith(): Add signature
     * - compact(): Build final token string
     *
     * @param claims Custom claims
     * @param subject Username
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * EXTRACT CUSTOM CLAIM
     *
     * Get custom data from token
     *
     * Example:
     * String role = jwtUtil.extractClaim(token, "role");
     *
     * @param token JWT token
     * @param claimName Claim key
     * @return Claim value (or null if not exists)
     */
    public Object extractClaim(String token, String claimName) {
        final Claims claims = extractAllClaims(token);
        return claims.get(claimName);
    }
}

/**
 * USAGE EXAMPLES
 *
 * 1. Login endpoint (generate token):
 * ```java
 * @PostMapping("/login")
 * public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
 *     // Authenticate user
 *     Authentication auth = authenticationManager.authenticate(
 *         new UsernamePasswordAuthenticationToken(
 *             request.getUsername(),
 *             request.getPassword()
 *         )
 *     );
 *
 *     // Get user details
 *     UserDetails userDetails = (UserDetails) auth.getPrincipal();
 *
 *     // Generate JWT
 *     String token = jwtUtil.generateToken(userDetails);
 *
 *     // Return token to client
 *     return ResponseEntity.ok(new AuthResponse(token));
 * }
 * ```
 *
 * 2. Validate token in filter:
 * ```java
 * // Extract token from header
 * String header = request.getHeader("Authorization");
 * String token = header.substring(7); // Remove "Bearer "
 *
 * // Get username from token
 * String username = jwtUtil.extractUsername(token);
 *
 * // Load user from database
 * UserDetails userDetails = userDetailsService.loadUserByUsername(username);
 *
 * // Validate token
 * if (jwtUtil.validateToken(token, userDetails)) {
 *     // Token valid - allow access
 *     SecurityContextHolder.getContext().setAuthentication(...);
 * } else {
 *     // Token invalid - deny access
 *     response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
 * }
 * ```
 *
 * 3. Client usage (JavaScript):
 * ```javascript
 * // Login
 * const response = await fetch('/api/auth/login', {
 *     method: 'POST',
 *     headers: {'Content-Type': 'application/json'},
 *     body: JSON.stringify({username: 'john', password: 'pass123'})
 * });
 * const {token} = await response.json();
 *
 * // Store token
 * localStorage.setItem('token', token);
 *
 * // Use token for API calls
 * fetch('/api/members', {
 *     headers: {
 *         'Authorization': `Bearer ${token}`
 *     }
 * });
 * ```
 *
 * SECURITY BEST PRACTICES
 *
 * 1. Secret Key:
 *    - Use strong secret (64+ characters)
 *    - Store in environment variable
 *    - Never commit to version control
 *    - Rotate periodically
 *
 * 2. Expiration:
 *    - Short expiration for sensitive data
 *    - Use refresh tokens for long sessions
 *    - Clear expired tokens
 *
 * 3. HTTPS:
 *    - Always use HTTPS in production
 *    - JWT sent over HTTP can be intercepted
 *    - Man-in-the-middle attacks
 *
 * 4. Storage:
 *    - Store token securely on client
 *    - HttpOnly cookies (prevents XSS)
 *    - Avoid localStorage if possible
 *
 * 5. Claims:
 *    - Don't store sensitive data in JWT
 *    - JWT is not encrypted, only signed
 *    - Anyone can decode and read claims
 *
 * REFRESH TOKENS
 *
 * For long-lived sessions:
 * ```java
 * // Generate access token (short-lived: 15 min)
 * String accessToken = jwtUtil.generateToken(userDetails, 900000);
 *
 * // Generate refresh token (long-lived: 7 days)
 * String refreshToken = jwtUtil.generateRefreshToken(userDetails, 604800000);
 *
 * // Client stores both
 * // When access token expires, use refresh token to get new access token
 * ```
 */
