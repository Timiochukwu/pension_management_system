package pension_management_system.pension.security;

/**
 * Role - User roles for access control
 *
 * Purpose: Define different user types with different permissions
 *
 * Roles:
 * - ADMIN: Full system access
 * - MANAGER: Approve benefits, view reports
 * - MEMBER: View own data, make contributions
 * - EMPLOYER: Manage employees, view reports
 *
 * Usage with Spring Security:
 * @PreAuthorize("hasRole('ADMIN')")
 * @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
 */
public enum Role {
    ROLE_ADMIN,      // System administrator
    ROLE_MANAGER,    // Pension manager
    ROLE_MEMBER,     // Pension member
    ROLE_EMPLOYER    // Employer representative
}
