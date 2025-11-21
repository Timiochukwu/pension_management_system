package pension_management_system.pension.contribution.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.member.entity.Member;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    // Renamed from findByMemberId - parameter is Member object, not ID
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
}
