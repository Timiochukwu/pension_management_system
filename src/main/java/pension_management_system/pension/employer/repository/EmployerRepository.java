package pension_management_system.pension.employer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pension_management_system.pension.employer.entity.Employer;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<Employer,Long> {
    Optional<Employer> findByEmployerId(String employerId);
    Optional<Employer> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByCompanyName(String companyName);
    List<Employer> findByActiveTrue();

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Employer e LEFT JOIN e.members m GROUP BY e ORDER BY COUNT(m) DESC")
    List<Employer> findTopEmployersByMemberCount();
}
