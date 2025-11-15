package pension_management_system.pension.member.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberSpecification {

    public static Specification<Member> filterMembers(
            String memberId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            MemberStatus status,
            Boolean active,
            Long employerId,
            LocalDate dateOfBirthFrom,
            LocalDate dateOfBirthTo,
            String city,
            String state,
            String country
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude soft-deleted records
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (memberId != null && !memberId.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("memberId")),
                        "%" + memberId.toLowerCase() + "%"
                ));
            }

            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + firstName.toLowerCase() + "%"
                ));
            }

            if (lastName != null && !lastName.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + lastName.toLowerCase() + "%"
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

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("memberStatus"), status));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            if (employerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("employer").get("id"), employerId));
            }

            if (dateOfBirthFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateOfBirth"), dateOfBirthFrom));
            }

            if (dateOfBirthTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateOfBirth"), dateOfBirthTo));
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

    public static Specification<Member> searchMembers(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("memberId")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                    criteriaBuilder.like(root.get("phoneNumber"), likePattern)
            );
        };
    }
}
