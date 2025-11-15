package pension_management_system.pension.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.security.entity.User;

import java.util.Optional;

/**
 * USER REPOSITORY
 *
 * Purpose: Database operations for User entity
 *
 * Methods:
 * - findByUsername - Find user by username for authentication
 * - findByEmail - Find user by email
 * - existsByUsername - Check if username is taken
 * - existsByEmail - Check if email is taken
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * Used for authentication
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     * Used for registration validation
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * Used for registration validation
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email or username
     * Useful for flexible login (email OR username)
     */
    @Query("SELECT u FROM User u WHERE u.email = ?1 OR u.username = ?1")
    Optional<User> findByEmailOrUsername(String emailOrUsername);
}
