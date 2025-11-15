package pension_management_system.pension.contribution.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.dto.ContributionStatementResponse;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ContributionService Interface
 *
 * Purpose: Defines contract for pension contribution operations
 * Handles contribution processing, tracking, and reporting
 */
public interface ContributionService {
    ContributionResponse processContribution(ContributionRequest request);
    List<ContributionResponse> getMemberContributions(Long memberId);
    ContributionResponse getContributionById(Long id);
    BigDecimal calculateTotalContributions(Long memberId);
    BigDecimal calculateTotalByType(Long memberId, ContributionType type);
    ContributionStatementResponse generateStatement(Long  memberId, LocalDate startDate, LocalDate endDate);
    List<ContributionResponse> getContributionsByPeriod(Long memberId, LocalDate startDate, LocalDate endDate);

    /**
     * Search and filter contributions with pagination
     *
     * Allows filtering by multiple criteria to find specific contributions
     *
     * @param referenceNumber Filter by reference number (partial match)
     * @param memberId Filter by member ID
     * @param contributionType Filter by type (MONTHLY or VOLUNTARY)
     * @param status Filter by status (PENDING, COMPLETED, FAILED, etc.)
     * @param paymentMethod Filter by payment method (BANK_TRANSFER, CASH, etc.)
     * @param amountFrom Minimum contribution amount
     * @param amountTo Maximum contribution amount
     * @param contributionDateFrom Start date for contribution date range
     * @param contributionDateTo End date for contribution date range
     * @param pageable Pagination settings
     * @return Page containing matching contributions
     */
    Page<ContributionResponse> searchContributions(
            String referenceNumber,
            Long memberId,
            ContributionType contributionType,
            ContributionStatus status,
            PaymentMethod paymentMethod,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            LocalDate contributionDateFrom,
            LocalDate contributionDateTo,
            Pageable pageable
    );

    /**
     * Quick search contributions by keyword
     *
     * Searches across reference number, member name, and member email
     *
     * @param searchTerm Keyword to search for
     * @param pageable Pagination settings
     * @return Page containing matching contributions
     */
    Page<ContributionResponse> quickSearch(String searchTerm, Pageable pageable);
}
