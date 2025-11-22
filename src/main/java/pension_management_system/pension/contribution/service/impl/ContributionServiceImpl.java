package pension_management_system.pension.contribution.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import pension_management_system.pension.contribution.mapper.ContributionMapper;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.contribution.service.ContributionService;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        contributionRepository.save(savedContribution);

        log.info("Contribution processed successfully for member id {}", request.getMemberId(), savedContribution.getReferenceNumber());

        return contributionMapper.toResponse(savedContribution);
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
        log.info("Fetching all contributions for member id {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));
        List<Contribution> contributions = contributionRepository.findByMember(member);

        return contributions.stream()
                .map(contributionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ContributionResponse getContributionById(Long id) {
        log.info("Fetching contribution for id {}", id);
        Contribution contribution = contributionRepository.findById(id)

                .orElseThrow(() -> new InvalidContributionException("Contribution not found with id: " + id));
        return contributionMapper.toResponse(contribution);
    }

    @Override
    public BigDecimal calculateTotalContributions(Long memberId) {
        log.info("Calculating total contributions for member id {}", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));
        log.debug("Calculating total for member ID: {}", member.getId());
        BigDecimal total = contributionRepository.getTotalContributionsById(memberId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalByType(Long memberId, ContributionType type) {
        log.info("Calculating total contributions for member id {}", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));
        BigDecimal total = contributionRepository.getTotalByMemberAndType(member, type);
        return total != null ? total : BigDecimal.ZERO;

    }

    @Override
    public ContributionStatementResponse generateStatement(Long memberId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating statement for member id {}", memberId);
        if (startDate.isAfter(endDate)) {
            throw new InvalidContributionException("Start date must be before end date");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));
        List<Contribution> contributions = contributionRepository.findByMemberAndContributionDateBetween(member, startDate, endDate);
        BigDecimal totalMontly = BigDecimal.ZERO;
        BigDecimal totalVoluntary = BigDecimal.ZERO;

        for (Contribution contribution : contributions) {
            if (contribution.getContributionType() == ContributionType.MONTHLY) {
                totalMontly = totalMontly.add(contribution.getContributionAmount());
            }else {
                totalVoluntary = totalVoluntary.add(contribution.getContributionAmount());
            }
        }
        BigDecimal totalContribution = totalMontly.add(totalVoluntary);

        List<ContributionResponse> contributionResponses = contributions.stream()
                .map(contributionMapper::toResponse)
                .collect(Collectors.toList());

        return ContributionStatementResponse.builder()
                .memberId(member.getMemberId())
                .memberName(member.getFullName())
                .startDate(startDate)
                .endDate(endDate)
                .contributions(contributionResponses)
                .totalMonthlyContribution(totalMontly)
                .totalVoluntaryContribution(totalVoluntary)
                .grandTotal(totalContribution)
                .numberOfContributions(contributions.size())
                .generatedDate(LocalDate.now())
                .build();
    }

    @Override
    public List<ContributionResponse> getContributionsByPeriod(Long memberId, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    @Override
    public Page<ContributionResponse> quickSearch(String keyword, Pageable pageable) {
        log.info("Quick search for keyword: {}", keyword);
        // Simple implementation - search all contributions and filter by keyword
        List<Contribution> allContributions = contributionRepository.findAll();

        List<ContributionResponse> filtered = allContributions.stream()
                .filter(c -> matchesKeyword(c, keyword))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .map(contributionMapper::toResponse)
                .collect(Collectors.toList());

        long total = allContributions.stream()
                .filter(c -> matchesKeyword(c, keyword))
                .count();

        return new PageImpl<>(filtered, pageable, total);
    }

    private boolean matchesKeyword(Contribution contribution, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String lowerKeyword = keyword.toLowerCase();
        return (contribution.getReferenceNumber() != null &&
                contribution.getReferenceNumber().toLowerCase().contains(lowerKeyword)) ||
               (contribution.getMember() != null && contribution.getMember().getMemberId() != null &&
                contribution.getMember().getMemberId().toLowerCase().contains(lowerKeyword)) ||
               (contribution.getMember() != null && contribution.getMember().getFullName() != null &&
                contribution.getMember().getFullName().toLowerCase().contains(lowerKeyword));
    }

    @Override
    public Page<ContributionResponse> getAllContributions(Pageable pageable) {
        log.info("Getting all contributions with pagination");
        Page<Contribution> contributionPage = contributionRepository.findAll(pageable);
        return contributionPage.map(contributionMapper::toResponse);
    }

    @Override
    public Page<ContributionResponse> searchContributions(
            String keyword,
            Long memberId,
            ContributionType type,
            ContributionStatus status,
            PaymentMethod paymentMethod,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        log.info("Searching contributions with filters");

        // Get all contributions and apply filters
        List<Contribution> allContributions = contributionRepository.findAll();

        List<ContributionResponse> filtered = allContributions.stream()
                .filter(c -> matchesSearchCriteria(c, keyword, memberId, type, status, paymentMethod, minAmount, maxAmount, startDate, endDate))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .map(contributionMapper::toResponse)
                .collect(Collectors.toList());

        long total = allContributions.stream()
                .filter(c -> matchesSearchCriteria(c, keyword, memberId, type, status, paymentMethod, minAmount, maxAmount, startDate, endDate))
                .count();

        return new PageImpl<>(filtered, pageable, total);
    }

    private boolean matchesSearchCriteria(
            Contribution c,
            String keyword,
            Long memberId,
            ContributionType type,
            ContributionStatus status,
            PaymentMethod paymentMethod,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDate startDate,
            LocalDate endDate) {

        // Keyword filter
        if (keyword != null && !keyword.trim().isEmpty() && !matchesKeyword(c, keyword)) {
            return false;
        }

        // Member ID filter
        if (memberId != null && (c.getMember() == null || !c.getMember().getId().equals(memberId))) {
            return false;
        }

        // Type filter
        if (type != null && c.getContributionType() != type) {
            return false;
        }

        // Status filter
        if (status != null && c.getStatus() != status) {
            return false;
        }

        // Payment method filter
        if (paymentMethod != null && c.getPaymentMethod() != paymentMethod) {
            return false;
        }

        // Amount range filter
        if (minAmount != null && c.getContributionAmount().compareTo(minAmount) < 0) {
            return false;
        }
        if (maxAmount != null && c.getContributionAmount().compareTo(maxAmount) > 0) {
            return false;
        }

        // Date range filter
        if (startDate != null && c.getContributionDate().isBefore(startDate.atStartOfDay())) {
            return false;
        }
        if (endDate != null && c.getContributionDate().isAfter(endDate.atTime(23, 59, 59))) {
            return false;
        }

        return true;
    }
}
