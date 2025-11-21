package pension_management_system.pension.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * RefreshTokenService - Manages refresh tokens for JWT authentication
 *
 * Refresh tokens allow users to get new access tokens without re-authenticating
 * Stored in Redis with configurable expiration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Value("${jwt.refresh-expiration:604800000}")  // 7 days default
    private Long refreshTokenExpiration;

    /**
     * Generate a new refresh token for a user
     *
     * @param username The username to generate token for
     * @return The generated refresh token
     */
    public String generateRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + refreshToken;

        // Store in Redis with expiration
        redisTemplate.opsForValue().set(key, username, Duration.ofMillis(refreshTokenExpiration));

        log.info("Generated refresh token for user: {}", username);
        return refreshToken;
    }

    /**
     * Validate refresh token and return new access token
     *
     * @param refreshToken The refresh token to validate
     * @return New JWT access token or null if invalid
     */
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String username = (String) redisTemplate.opsForValue().get(key);

        if (username == null) {
            log.warn("Invalid or expired refresh token");
            return null;
        }

        // Load user and generate new access token
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtUtil.generateToken(userDetails);

        // Optionally rotate refresh token
        String newRefreshToken = rotateRefreshToken(refreshToken, username);

        log.info("Refreshed access token for user: {}", username);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    /**
     * Rotate refresh token - invalidate old one and create new
     * Enhances security by limiting token lifetime
     */
    private String rotateRefreshToken(String oldToken, String username) {
        // Delete old token
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + oldToken);

        // Generate new token
        return generateRefreshToken(username);
    }

    /**
     * Revoke a refresh token (logout)
     *
     * @param refreshToken The token to revoke
     */
    public void revokeRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Revoked refresh token");
        }
    }

    /**
     * Revoke all refresh tokens for a user
     * Useful when user changes password or security concern
     *
     * @param username The user whose tokens to revoke
     */
    public void revokeAllUserTokens(String username) {
        // In a production system, you'd maintain a set of tokens per user
        // For now, this is a placeholder for such functionality
        log.info("Revoking all refresh tokens for user: {}", username);
    }

    /**
     * Response DTO for token refresh
     */
    public record TokenRefreshResponse(String accessToken, String refreshToken) {}
}
