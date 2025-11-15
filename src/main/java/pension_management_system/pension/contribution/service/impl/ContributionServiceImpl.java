package pension_management_system.pension.contribution.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Transactional(readOnly = true)
    public List<ContributionResponse> getMemberContributions(Long memberId) {
        log.info("Fetching all contributions for member id: {}", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        List<Contribution> contributions = contributionRepository.findByMemberId(member);
        return contributions.stream()
                .map(contributionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContributionResponse getContributionById(Long id) {
        log.info("Fetching contribution by id: {}", id);
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new InvalidContributionException("Contribution not found with id: " + id));
        return contributionMapper.toResponse(contribution);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalContributions(Long memberId) {
        log.info("Calculating total contributions for member id: {}", memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        BigDecimal total = contributionRepository.getTotalContributionsByMember(member);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalByType(Long memberId, ContributionType type) {
        log.info("Calculating total {} contributions for member id: {}", type, memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        BigDecimal total = contributionRepository.getTotalByMemberAndType(member, type);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public ContributionStatementResponse generateStatement(Long memberId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating statement for member id: {} from {} to {}", memberId, startDate, endDate);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        List<Contribution> contributions = contributionRepository
                .findByMemberAndContributionDateBetween(member, startDate, endDate);

        List<ContributionResponse> contributionResponses = contributions.stream()
                .map(contributionMapper::toResponse)
                .toList();

        BigDecimal totalMonthly = contributions.stream()
                .filter(c -> c.getContributionType() == ContributionType.MONTHLY)
                .map(Contribution::getContributionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVoluntary = contributions.stream()
                .filter(c -> c.getContributionType() == ContributionType.VOLUNTARY)
                .map(Contribution::getContributionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandTotal = totalMonthly.add(totalVoluntary);

        return ContributionStatementResponse.builder()
                .memberId(member.getMemberId())
                .memberName(member.getFirstName() + " " + member.getLastName())
                .startDate(startDate)
                .endDate(endDate)
                .generatedDate(LocalDate.now())
                .contributions(contributionResponses)
                .numberOfContributions(contributionResponses.size())
                .totalMonthlyContribution(totalMonthly)
                .totalVoluntaryContribution(totalVoluntary)
                .grandTotal(grandTotal)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContributionResponse> getContributionsByPeriod(Long memberId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching contributions for member id: {} from {} to {}", memberId, startDate, endDate);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        List<Contribution> contributions = contributionRepository
                .findByMemberAndContributionDateBetween(member, startDate, endDate);

        return contributions.stream()
                .map(contributionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ContributionResponse updateContribution(Long id, ContributionRequest request) {
        log.info("Updating contribution with id: {}", id);
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new InvalidContributionException("Contribution not found with id: " + id));

        // Validate member exists
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + request.getMemberId()));

        // Validate amount
        if (request.getContributionAmount().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new InvalidContributionException("Contribution amount must be at least 100");
        }

        // If changing to monthly contribution or changing the date of a monthly contribution,
        // validate no duplicate monthly contribution
        if (request.getContributionType() == ContributionType.MONTHLY) {
            // Check if the month/year is changing
            boolean isDateChanging = !contribution.getContributionDate().equals(request.getContributionDate());
            boolean isTypeChanging = contribution.getContributionType() != ContributionType.MONTHLY;

            if (isDateChanging || isTypeChanging) {
                int year = request.getContributionDate().getYear();
                int month = request.getContributionDate().getMonthValue();

                Optional<Contribution> existingContribution = contributionRepository
                        .findMonthlyContributionByMemberAndYearMonth(member, ContributionType.MONTHLY, year, month);

                if (existingContribution.isPresent() && !existingContribution.get().getId().equals(id)) {
                    String errorMessage = String.format(
                            "Member %s already has a monthly contribution for %d-%02d. Reference: %s",
                            member.getMemberId(), year, month, existingContribution.get().getReferenceNumber()
                    );
                    throw new DuplicateMonthlyContributionException(errorMessage);
                }
            }
        }

        // Update fields
        contribution.setMember(member);
        contribution.setContributionType(request.getContributionType());
        contribution.setContributionAmount(request.getContributionAmount());
        contribution.setContributionDate(request.getContributionDate());
        contribution.setPaymentMethod(request.getPaymentMethod());
        contribution.setDescription(request.getDescription());

        Contribution updatedContribution = contributionRepository.save(contribution);
        log.info("Contribution updated successfully with id: {}", id);

        return contributionMapper.toResponse(updatedContribution);
    }

    @Override
    @Transactional
    public void deleteContribution(Long id) {
        log.info("Deleting contribution with id: {}", id);
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new InvalidContributionException("Contribution not found with id: " + id));

        contributionRepository.delete(contribution);
        log.info("Contribution deleted successfully with id: {}", id);
    }
}
