package pension_management_system.pension.contribution.specification;

import org.springframework.data.jpa.domain.Specification;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.entity.ContributionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ContributionSpecification - JPA Specifications for dynamic queries
 */
public class ContributionSpecification {

    public static Specification<Contribution> hasStatus(ContributionStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Contribution> hasType(ContributionType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("contributionType"), type);
        };
    }

    public static Specification<Contribution> hasMemberId(Long memberId) {
        return (root, query, criteriaBuilder) -> {
            if (memberId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("member").get("id"), memberId);
        };
    }

    public static Specification<Contribution> hasEmployerId(Long employerId) {
        return (root, query, criteriaBuilder) -> {
            if (employerId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("employer").get("id"), employerId);
        };
    }

    public static Specification<Contribution> createdAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<Contribution> createdBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<Contribution> amountGreaterThan(BigDecimal amount) {
        return (root, query, criteriaBuilder) -> {
            if (amount == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), amount);
        };
    }

    public static Specification<Contribution> amountLessThan(BigDecimal amount) {
        return (root, query, criteriaBuilder) -> {
            if (amount == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("amount"), amount);
        };
    }

    public static Specification<Contribution> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }

    /**
     * Filter contributions with multiple criteria
     */
    public static Specification<Contribution> filterContributions(
            String referenceNumber,
            Long memberId,
            ContributionType contributionType,
            ContributionStatus status,
            pension_management_system.pension.contribution.entity.PaymentMethod paymentMethod,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            java.time.LocalDate contributionDateFrom,
            java.time.LocalDate contributionDateTo
    ) {
        return (root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (referenceNumber != null && !referenceNumber.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("referenceNumber")),
                        "%" + referenceNumber.toLowerCase() + "%"
                ));
            }

            if (memberId != null) {
                predicates.add(criteriaBuilder.equal(root.get("member").get("id"), memberId));
            }

            if (contributionType != null) {
                predicates.add(criteriaBuilder.equal(root.get("contributionType"), contributionType));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (paymentMethod != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), paymentMethod));
            }

            if (amountFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("contributionAmount"), amountFrom));
            }

            if (amountTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("contributionAmount"), amountTo));
            }

            if (contributionDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("contributionDate"), contributionDateFrom));
            }

            if (contributionDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("contributionDate"), contributionDateTo));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * Search contributions by keyword across multiple fields
     */
    public static Specification<Contribution> searchContributions(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("referenceNumber")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("member").get("firstName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("member").get("lastName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("member").get("email")), pattern)
            );
        };
    }
}
