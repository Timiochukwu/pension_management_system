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
        log.info("ssss"+ member.getId());
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
                .totalMonthlyContribution(totalVoluntary)
                .grandTotal(totalContribution)
                .numberOfContributions(contributions.size())
                .generatedDate(LocalDate.now())
                .build();
    }

    @Override
    public List<ContributionResponse> getContributionsByPeriod(Long memberId, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }
}
