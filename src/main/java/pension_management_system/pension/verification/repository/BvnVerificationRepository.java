package pension_management_system.pension.verification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.verification.entity.BvnVerification;

import java.util.Optional;

@Repository
public interface BvnVerificationRepository extends JpaRepository<BvnVerification, Long> {

    Optional<BvnVerification> findByMemberId(Long memberId);

    Optional<BvnVerification> findByBvnNumber(String bvnNumber);

    boolean existsByMemberId(Long memberId);
}
