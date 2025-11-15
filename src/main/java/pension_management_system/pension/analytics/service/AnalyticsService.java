package pension_management_system.pension.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.analytics.dto.MemberAnalyticsDto;
import pension_management_system.pension.analytics.dto.SystemStatisticsDto;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.repository.BenefitRepository;
import pension_management_system.pension.common.exception.MemberNotFoundException;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.employer.repository.EmployerRepository;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.entity.MemberStatus;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final BenefitRepository benefitRepository;
    private final EmployerRepository employerRepository;

    @Transactional(readOnly = true)
    public SystemStatisticsDto getSystemStatistics() {
        log.info("Fetching system statistics");

        Long totalMembers = memberRepository.count();
        Long activeMembers = memberRepository.countByMemberStatusAndActiveTrue(MemberStatus.ACTIVE);
        Long retiredMembers = memberRepository.countByMemberStatusAndActiveTrue(MemberStatus.RETIRED);

        Long totalContributions = contributionRepository.count();
        BigDecimal totalContributionAmount = contributionRepository.findAll().stream()
                .map(c -> c.getContributionAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalBenefits = benefitRepository.count();
        Long pendingBenefits = (long) benefitRepository.findByStatus(BenefitStatus.PENDING).size();
        Long approvedBenefits = (long) benefitRepository.findByStatus(BenefitStatus.APPROVED).size();
        Long disbursedBenefits = (long) benefitRepository.findByStatus(BenefitStatus.DISBURSED).size();

        BigDecimal totalBenefitsAmount = benefitRepository.findAll().stream()
                .filter(b -> b.getStatus() == BenefitStatus.DISBURSED)
                .map(b -> b.getNetPayable())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalEmployers = employerRepository.count();
        Long activeEmployers = employerRepository.findAll().stream()
                .filter(e -> e.getActive())
                .count();

        return SystemStatisticsDto.builder()
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .retiredMembers(retiredMembers)
                .totalContributions(totalContributions)
                .totalContributionAmount(totalContributionAmount)
                .totalBenefits(totalBenefits)
                .pendingBenefits(pendingBenefits)
                .approvedBenefits(approvedBenefits)
                .disbursedBenefits(disbursedBenefits)
                .totalBenefitsAmount(totalBenefitsAmount)
                .totalEmployers(totalEmployers)
                .activeEmployers(activeEmployers)
                .build();
    }

    @Transactional(readOnly = true)
    public MemberAnalyticsDto getMemberAnalytics(Long memberId) {
        log.info("Fetching analytics for member: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        // Calculate years of service
        Period servicePeriod = Period.between(member.getCreatedAt().toLocalDate(), LocalDate.now());
        int yearsOfService = servicePeriod.getYears();

        // Get contributions
        Long totalContributionsCount = contributionRepository.countByMember(member);

        BigDecimal totalMonthly = contributionRepository.getTotalByMemberAndType(member, ContributionType.MONTHLY);
        BigDecimal totalVoluntary = contributionRepository.getTotalByMemberAndType(member, ContributionType.VOLUNTARY);

        totalMonthly = totalMonthly != null ? totalMonthly : BigDecimal.ZERO;
        totalVoluntary = totalVoluntary != null ? totalVoluntary : BigDecimal.ZERO;

        BigDecimal totalContributionAmount = totalMonthly.add(totalVoluntary);

        // Estimate benefit (simple calculation: contributions + 10% employer + 8% returns per year)
        BigDecimal employerContribution = totalContributionAmount.multiply(BigDecimal.valueOf(0.10));
        BigDecimal returns = totalContributionAmount.add(employerContribution)
                .multiply(BigDecimal.valueOf(0.08))
                .multiply(BigDecimal.valueOf(yearsOfService));
        BigDecimal estimatedBenefit = totalContributionAmount.add(employerContribution).add(returns);

        return MemberAnalyticsDto.builder()
                .memberId(member.getMemberId())
                .memberName(member.getFirstName() + " " + member.getLastName())
                .yearsOfService(yearsOfService)
                .totalContributions(totalContributionsCount)
                .totalMonthlyContributions(totalMonthly)
                .totalVoluntaryContributions(totalVoluntary)
                .totalContributionAmount(totalContributionAmount)
                .estimatedBenefit(estimatedBenefit)
                .memberStatus(member.getMemberStatus().name())
                .build();
    }
}
