package pension_management_system.pension.contribution.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.entity.PaymentMethod;
import pension_management_system.pension.member.entity.Member;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    // Keep old method name for backward compatibility
    List<Contribution> findByMemberId(Member member);

    // New method with clearer name
    List<Contribution> findAllByMember(Member member);

    List <Contribution> findByMemberAndContributionType(Member member, ContributionType contributionType);
    @Query("SELECT c FROM Contribution c WHERE c.member = :member " +
            "AND c.contributionType = :type " +
            "AND YEAR(c.contributionDate) = :year " +
            "AND MONTH(c.contributionDate) = :month")
    Optional<Contribution> findMonthlyContributionByMemberAndYearMonth(
        @Param("member") Member member,
        @Param("type") ContributionType type,
        @Param("year") int year,
        @Param("month")  int month
    );

    @Query("SELECT SUM(c.contributionAmount) FROM Contribution c WHERE c.member =:member")
    BigDecimal getTotalContributionsByMember(
            @Param("member") Member member
    );
    @Query("SELECT SUM(c.contributionAmount) FROM Contribution c WHERE c.member.id =:id")
    BigDecimal getTotalContributionsById(
            @Param("id")  Long id
    );

    @Query("SELECT SUM(c.contributionAmount) FROM Contribution c WHERE c.member =:member " +
            "AND c.contributionType =:type")
    BigDecimal getTotalByMemberAndType(
            @Param("member") Member member,
            @Param("type") ContributionType type
    );

    List<Contribution> findByMemberAndContributionDateBetween(
            Member member, LocalDate startDate, LocalDate endDate
    );

    long countByMember(Member member);

    List<Contribution> findByMember(Member member);

    // Additional methods for analytics
    @Query("SELECT SUM(c.contributionAmount) FROM Contribution c")
    BigDecimal getTotalContributionAmount();

    @Query("SELECT SUM(c.contributionAmount) FROM Contribution c WHERE c.contributionType = :type")
    BigDecimal getTotalByType(@Param("type") ContributionType type);

    long countByStatus(ContributionStatus status);

    @Query("SELECT c FROM Contribution c WHERE c.contributionDate >= :date")
    List<Contribution> findContributionsSinceDate(@Param("date") LocalDateTime date);

    long countByPaymentMethod(PaymentMethod paymentMethod);

    @Query("SELECT SUM(c.contributionAmount) FROM Contribution c WHERE c.paymentMethod = :method")
    BigDecimal getTotalAmountByPaymentMethod(@Param("method") PaymentMethod method);

    @Query("SELECT c FROM Contribution c WHERE c.member.id = :memberId " +
            "AND c.contributionType = :type " +
            "AND YEAR(c.contributionDate) = :year " +
            "AND MONTH(c.contributionDate) = :month")
    Optional<Contribution> findMonthlyContributionByMemberIdAndYearMonth(
        @Param("memberId") Long memberId,
        @Param("type") ContributionType type,
        @Param("year") int year,
        @Param("month") int month
    );
}
