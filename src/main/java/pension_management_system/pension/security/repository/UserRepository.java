package pension_management_system.pension.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.security.entity.User;

import java.util.Optional;

/**
 * UserRepository - Repository for User entity in security module
 */
@Repository("securityUserRepository")
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);
}
