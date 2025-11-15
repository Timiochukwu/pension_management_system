package pension_management_system.pension.contribution.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.common.exception.DuplicateMonthlyContributionException;
import pension_management_system.pension.common.exception.InvalidContributionException;
import pension_management_system.pension.common.exception.MemberNotFoundException;
import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.dto.ContributionStatementResponse;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.entity.PaymentMethod;
import pension_management_system.pension.contribution.mapper.ContributionMapper;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.contribution.service.ContributionService;
import pension_management_system.pension.contribution.specification.ContributionSpecification;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContributionServiceImpl implements ContributionService {
    private final ContributionRepository contributionRepository;
    private final MemberRepository memberRepository;
    private final ContributionMapper contributionMapper;


    @Override
    @Transactional
    public ContributionResponse processContribution(ContributionRequest request) {
        log.info("Processing Contribution request for member id  {}", request.getMemberId());

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + request.getMemberId()));
        if (!member.getActive()) {
            throw new InvalidContributionException("Cannot process contribution for non-active member");
        }
        if (request.getContributionAmount().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new InvalidContributionException("Contribution amount must be greater than zero");
        }
        if (request.getContributionType() == ContributionType.MONTHLY) {
            validateMonthlyContribution(member, request.getContributionDate());
        }
        Contribution contribution = contributionMapper.toEntity(request);
        contribution.setMember(member);
        contribution.setStatus(ContributionStatus.PENDING);

        Contribution savedContribution = contributionRepository.save(contribution);
        savedContribution.markAsProcessed("SYSTEM");

        contributionRepository.save(contribution);

        log.info("Contribution processed successfully for member id {}", request.getMemberId(), savedContribution.getReferenceNumber());

        return contributionMapper.toResponse(contribution);
    }
    private void validateMonthlyContribution(Member member, LocalDate contributionDate) {
        int year = contributionDate.getYear();
        int month = contributionDate.getMonthValue();

        Optional<Contribution> existingContribution = contributionRepository
                .findMonthlyContributionByMemberAndYearMonth(member, ContributionType.MONTHLY, year, month);
        if (existingContribution.isPresent()) {
            String errorMessage = String.format(
                    "Member %s already has a monthly contribution for %d-%02d. Reference: %s",
                    member.getMemberId(), year, month, existingContribution.get().getReferenceNumber()
            );
            log.error(errorMessage);
            throw new DuplicateMonthlyContributionException(errorMessage);
        }
    }


    @Override
    public List<ContributionResponse> getMemberContributions(Long memberId) {
        return List.of();
    }

    @Override
    public ContributionResponse getContributionById(Long id) {
        return null;
    }

    @Override
    public BigDecimal calculateTotalContributions(Long memberId) {
        return null;
    }

    @Override
    public BigDecimal calculateTotalByType(Long memberId, ContributionType type) {
        return null;
    }

    @Override
    public ContributionStatementResponse generateStatement(Long memberId, LocalDate startDate, LocalDate endDate) {
        return null;
    }

    @Override
    public List<ContributionResponse> getContributionsByPeriod(Long memberId, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    /**
     * ADVANCED SEARCH - Search contributions with multiple filter criteria
     *
     * How this works:
     * 1. Builds dynamic query using Specification pattern
     * 2. Combines all filter criteria into SQL WHERE clauses
     * 3. Executes query with pagination
     * 4. Maps entities to DTOs
     *
     * Use cases:
     * - Find all PENDING contributions for a specific member
     * - Find all contributions within amount range (e.g., 1000-5000)
     * - Find all BANK_TRANSFER contributions in last 3 months
     * - Combine multiple filters for complex searches
     *
     * Example SQL generated:
     * WHERE member_id = 123
     *   AND status = 'COMPLETED'
     *   AND contribution_date BETWEEN '2025-01-01' AND '2025-12-31'
     *   AND contribution_amount >= 1000
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ContributionResponse> searchContributions(
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
    ) {
        // Log for debugging/monitoring
        log.info("Searching contributions - memberId: {}, status: {}", memberId, status);

        // STEP 1: Build dynamic query specification
        // Only non-null parameters are added to the query
        Specification<Contribution> spec = ContributionSpecification.filterContributions(
                referenceNumber, memberId, contributionType, status, paymentMethod,
                amountFrom, amountTo, contributionDateFrom, contributionDateTo
        );

        // STEP 2: Execute query with pagination
        Page<Contribution> contributions = contributionRepository.findAll(spec, pageable);

        // STEP 3: Convert entities to DTOs
        // This hides internal database structure from API consumers
        return contributions.map(contributionMapper::toResponse);
    }

    /**
     * QUICK SEARCH - Simple keyword search across multiple fields
     *
     * Searches in:
     * - Reference number (e.g., "CON20250115...")
     * - Member first name
     * - Member last name
     * - Member email
     *
     * Perfect for: Search boxes where user types one term and you search everywhere
     *
     * Example: searchTerm="john"
     * Finds contributions where:
     * - Reference contains "john" OR
     * - Member first name contains "john" OR
     * - Member last name contains "john" OR
     * - Member email contains "john"
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ContributionResponse> quickSearch(String searchTerm, Pageable pageable) {
        log.info("Quick search for contributions with term: {}", searchTerm);

        // Build OR-based search specification
        Specification<Contribution> spec = ContributionSpecification.searchContributions(searchTerm);

        // Execute and return results
        Page<Contribution> contributions = contributionRepository.findAll(spec, pageable);

        return contributions.map(contributionMapper::toResponse);
    }
}
