package pension_management_system.pension.employer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.employer.entity.Employer;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<Employer,Long>, JpaSpecificationExecutor<Employer> {
    Optional<Employer> findByEmployerId(String employerId);
    Optional<Employer> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByCompanyName(String companyName);
    List<Employer> findByActiveTrue();

    // Analytics queries
    @Query("SELECT e FROM Employer e ORDER BY SIZE(e.members) DESC")
    List<Employer> findTopEmployersByMemberCount();
}
