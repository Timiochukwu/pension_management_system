package pension_management_system.pension.benefit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.benefit.entity.Benefit;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.entity.BenefitType;
import pension_management_system.pension.member.entity.Member;

import java.math.BigDecimal;
import java.util.List;

/**
 * BenefitRepository - Database operations for Benefit entity
 *
 * Purpose: Provides methods to query and manipulate benefit data
 * Spring Data JPA automatically implements these methods - no SQL needed!
 *
 * What is a Repository?
 * - Interface between application and database
 * - Provides CRUD operations (Create, Read, Update, Delete)
 * - Allows custom queries for specific business needs
 *
 * How it works:
 * - You declare method signatures
 * - Spring Data JPA generates the SQL automatically
 * - Method names follow naming conventions (findBy, countBy, etc.)
 *
 * @Repository - Marks this as a Spring repository component
 * JpaRepository<Benefit, Long> - Basic CRUD methods for Benefit with ID type Long
 * JpaSpecificationExecutor - Enables dynamic queries for advanced search
 */
@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long>, JpaSpecificationExecutor<Benefit> {

    /**
     * Find all benefits for a specific member
     *
     * SQL Generated:
     * SELECT * FROM benefits WHERE member_id = ?
     *
     * Use case: Show member's benefit claim history
     *
     * @param member The member whose benefits to find
     * @return List of all benefits for that member
     */
    List<Benefit> findByMember(Member member);

    /**
     * Find benefits by member and status
     *
     * SQL Generated:
     * SELECT * FROM benefits WHERE member_id = ? AND status = ?
     *
     * Use case: Find all pending claims for a member
     *
     * @param member The member
     * @param status The status (PENDING, APPROVED, etc.)
     * @return List of matching benefits
     */
    List<Benefit> findByMemberAndStatus(Member member, BenefitStatus status);

    /**
     * Find benefits by member and type
     *
     * SQL Generated:
     * SELECT * FROM benefits WHERE member_id = ? AND benefit_type = ?
     *
     * Use case: Find all retirement benefits for a member
     */
    List<Benefit> findByMemberAndBenefitType(Member member, BenefitType benefitType);

    /**
     * Find all benefits with specific status
     *
     * SQL Generated:
     * SELECT * FROM benefits WHERE status = ?
     *
     * Use case: Admin wants to see all pending claims to review
     */
    List<Benefit> findByStatus(BenefitStatus status);

    /**
     * Find all benefits of specific type
     *
     * SQL Generated:
     * SELECT * FROM benefits WHERE benefit_type = ?
     *
     * Use case: Report on all retirement benefits
     */
    List<Benefit> findByBenefitType(BenefitType benefitType);

    /**
     * Count benefits by status
     *
     * SQL Generated:
     * SELECT COUNT(*) FROM benefits WHERE status = ?
     *
     * Use case: Dashboard showing "5 pending claims"
     */
    long countByStatus(BenefitStatus status);

    /**
     * Count benefits for a member
     *
     * SQL Generated:
     * SELECT COUNT(*) FROM benefits WHERE member_id = ?
     *
     * Use case: Show how many benefits a member has claimed
     */
    long countByMember(Member member);

    /**
     * Calculate total approved amount for a member
     *
     * Uses @Query annotation for custom JPQL (like SQL but for entities)
     *
     * JPQL Explained:
     * - Similar to SQL but uses entity names and properties
     * - SELECT SUM(b.approvedAmount) - Add up all approved amounts
     * - FROM Benefit b - From Benefit entity (alias 'b')
     * - WHERE b.member = :member - Filter by specific member
     * - AND b.status = 'PAID' - Only count paid benefits
     *
     * Use case: Show member's total lifetime benefits received
     *
     * @param member The member
     * @return Total paid amount (null if no paid benefits)
     */
    @Query("SELECT SUM(b.approvedAmount) FROM Benefit b WHERE b.member = :member AND b.status = 'PAID'")
    BigDecimal getTotalPaidBenefits(@Param("member") Member member);

    /**
     * Calculate total approved amount by type
     *
     * Use case: Report showing total retirement benefits paid
     */
    @Query("SELECT SUM(b.approvedAmount) FROM Benefit b WHERE b.benefitType = :type AND b.status = 'PAID'")
    BigDecimal getTotalByTypeAndPaid(@Param("type") BenefitType type);

    /**
     * Find pending benefits for review
     *
     * Use case: Admin dashboard showing claims waiting for review
     */
    @Query("SELECT b FROM Benefit b WHERE b.status = 'PENDING' OR b.status = 'UNDER_REVIEW' ORDER BY b.applicationDate ASC")
    List<Benefit> findPendingReviews();

    /**
     * Find benefits by status ordered by application date
     *
     * Use case: Process claims in order they were received (FIFO)
     */
    List<Benefit> findByStatusOrderByApplicationDateAsc(BenefitStatus status);
}
