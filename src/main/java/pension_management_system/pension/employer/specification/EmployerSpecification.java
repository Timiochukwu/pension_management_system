package pension_management_system.pension.employer.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pension_management_system.pension.employer.entity.Employer;

import java.util.ArrayList;
import java.util.List;

public class EmployerSpecification {

    public static Specification<Employer> filterEmployers(
            String employerId,
            String companyName,
            String registrationNumber,
            String email,
            String phoneNumber,
            String industry,
            Boolean active,
            String city,
            String state,
            String country
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude soft-deleted records
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (employerId != null && !employerId.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("employerId")),
                        "%" + employerId.toLowerCase() + "%"
                ));
            }

            if (companyName != null && !companyName.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("companyName")),
                        "%" + companyName.toLowerCase() + "%"
                ));
            }

            if (registrationNumber != null && !registrationNumber.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("registrationNumber")),
                        "%" + registrationNumber.toLowerCase() + "%"
                ));
            }

            if (email != null && !email.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"
                ));
            }

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("phoneNumber"),
                        "%" + phoneNumber + "%"
                ));
            }

            if (industry != null && !industry.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("industry")),
                        "%" + industry.toLowerCase() + "%"
                ));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            if (city != null && !city.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("city")),
                        "%" + city.toLowerCase() + "%"
                ));
            }

            if (state != null && !state.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("state")),
                        "%" + state.toLowerCase() + "%"
                ));
            }

            if (country != null && !country.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("country")),
                        "%" + country.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Employer> searchEmployers(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("employerId")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("registrationNumber")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                    criteriaBuilder.like(root.get("phoneNumber"), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("industry")), likePattern)
            );
        };
    }
}
