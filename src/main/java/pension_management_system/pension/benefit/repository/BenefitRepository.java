package pension_management_system.pension.benefit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pension_management_system.pension.benefit.entity.Benefit;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.entity.BenefitType;
import pension_management_system.pension.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface BenefitRepository extends JpaRepository<Benefit, Long> {

    Optional<Benefit> findByReferenceNumber(String referenceNumber);

    List<Benefit> findByMember(Member member);

    List<Benefit> findByMemberAndStatus(Member member, BenefitStatus status);

    List<Benefit> findByStatus(BenefitStatus status);

    List<Benefit> findByBenefitType(BenefitType benefitType);

    @Query("SELECT COUNT(b) FROM Benefit b WHERE b.member = :member AND b.status = :status")
    long countByMemberAndStatus(@Param("member") Member member, @Param("status") BenefitStatus status);

    @Query("SELECT b FROM Benefit b WHERE b.member = :member AND b.benefitType = :type")
    List<Benefit> findByMemberAndBenefitType(@Param("member") Member member, @Param("type") BenefitType type);

    boolean existsByMemberAndStatus(Member member, BenefitStatus status);
}
