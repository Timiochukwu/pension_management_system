package pension_management_system.pension.contribution.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContributionSpecification {

    public static Specification<Contribution> filterContributions(
            String referenceNumber,
            Long memberId,
            ContributionType contributionType,
            ContributionStatus status,
            PaymentMethod paymentMethod,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            LocalDate contributionDateFrom,
            LocalDate contributionDateTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

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

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Contribution> searchContributions(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("referenceNumber")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("member").get("firstName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("member").get("lastName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("member").get("email")), likePattern)
            );
        };
    }
}
