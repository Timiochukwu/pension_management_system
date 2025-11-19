package pension_management_system.pension.analytics.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pension_management_system.pension.analytics.dto.*;
import pension_management_system.pension.analytics.service.AnalyticsService;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.entity.PaymentMethod;
import pension_management_system.pension.contribution.repository.ContributionRepository;
import pension_management_system.pension.employer.entity.Employer;
import pension_management_system.pension.employer.repository.EmployerRepository;
import pension_management_system.pension.member.entity.MemberStatus;
import pension_management_system.pension.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final MemberRepository memberRepository;
    private final EmployerRepository employerRepository;
    private final ContributionRepository contributionRepository;

    @Override
    @Cacheable(value = "dashboardStatistics", unless = "#result == null")
    public DashboardStatisticsResponse getDashboardStatistics() {
        log.info("Generating dashboard statistics");

        Long totalMembers = memberRepository.count();
        Long activeMembers = memberRepository.countByActiveTrue();
        Long inactiveMembers = memberRepository.countByActiveFalse();
        Long totalEmployers = employerRepository.count();
        Long totalContributions = contributionRepository.count();

        BigDecimal totalContributionAmount = contributionRepository.getTotalContributionAmount();
        if (totalContributionAmount == null) totalContributionAmount = BigDecimal.ZERO;

        BigDecimal monthlyAmount = contributionRepository.getTotalByType(ContributionType.MONTHLY);
        if (monthlyAmount == null) monthlyAmount = BigDecimal.ZERO;

        BigDecimal voluntaryAmount = contributionRepository.getTotalByType(ContributionType.VOLUNTARY);
        if (voluntaryAmount == null) voluntaryAmount = BigDecimal.ZERO;

        Long pendingContributions = contributionRepository.countByStatus(ContributionStatus.PENDING);
        Long completedContributions = contributionRepository.countByStatus(ContributionStatus.COMPLETED);
        Long failedContributions = contributionRepository.countByStatus(ContributionStatus.FAILED);

        return DashboardStatisticsResponse.builder()
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .inactiveMembers(inactiveMembers)
                .totalEmployers(totalEmployers)
                .totalContributions(totalContributions)
                .totalContributionAmount(totalContributionAmount)
                .monthlyContributionAmount(monthlyAmount)
                .voluntaryContributionAmount(voluntaryAmount)
                .pendingContributions(pendingContributions)
                .completedContributions(completedContributions)
                .failedContributions(failedContributions)
                .build();
    }

    @Override
    @Cacheable(value = "contributionTrend", key = "#months", unless = "#result == null")
    public ContributionTrendResponse getContributionTrend(int months) {
        log.info("Generating contribution trend for last {} months", months);

        LocalDate startDate = LocalDate.now().minusMonths(months);
        List<Contribution> contributions = contributionRepository.findContributionsSinceDate(startDate.atStartOfDay());

        List<ContributionTrendResponse.MonthlyData> monthlyData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = currentDate.minusMonths(i);
            int year = monthDate.getYear();
            int month = monthDate.getMonthValue();

            List<Contribution> monthContributions = contributions.stream()
                    .filter(c -> c.getContributionDate().getYear() == year &&
                            c.getContributionDate().getMonthValue() == month)
                    .collect(Collectors.toList());

            BigDecimal totalAmount = monthContributions.stream()
                    .map(Contribution::getContributionAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyData.add(ContributionTrendResponse.MonthlyData.builder()
                    .month(monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .year(year)
                    .count((long) monthContributions.size())
                    .amount(totalAmount)
                    .build());
        }

        return ContributionTrendResponse.builder()
                .monthlyTrend(monthlyData)
                .build();
    }

    @Override
    @Cacheable(value = "memberStatusDistribution", unless = "#result == null")
    public MemberStatusDistribution getMemberStatusDistribution() {
        log.info("Generating member status distribution");

        Long totalMembersCount = memberRepository.count();
        final Long totalMembers = (totalMembersCount == 0) ? 1L : totalMembersCount; // Prevent division by zero

        List<MemberStatusDistribution.StatusData> distribution = Arrays.stream(MemberStatus.values())
                .map(status -> {
                    Long count = memberRepository.countByMemberStatus(status);
                    Double percentage = (count * 100.0) / totalMembers;

                    return MemberStatusDistribution.StatusData.builder()
                            .status(status.name())
                            .count(count)
                            .percentage(BigDecimal.valueOf(percentage)
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue())
                            .build();
                })
                .collect(Collectors.toList());

        return MemberStatusDistribution.builder()
                .distribution(distribution)
                .build();
    }

    @Override
    @Cacheable(value = "contributionByPaymentMethod", unless = "#result == null")
    public ContributionByPaymentMethod getContributionByPaymentMethod() {
        log.info("Generating contribution by payment method statistics");

        Long totalContributions = contributionRepository.count();
        if (totalContributions == 0) {
            totalContributions = 1L; // Prevent division by zero
        }

        final Long finalTotalContributions = totalContributions;

        List<ContributionByPaymentMethod.PaymentMethodData> paymentMethods = Arrays.stream(PaymentMethod.values())
                .map(method -> {
                    Long count = contributionRepository.countByPaymentMethod(method);
                    BigDecimal totalAmount = contributionRepository.getTotalAmountByPaymentMethod(method);
                    if (totalAmount == null) totalAmount = BigDecimal.ZERO;

                    Double percentage = (count * 100.0) / finalTotalContributions;

                    return ContributionByPaymentMethod.PaymentMethodData.builder()
                            .paymentMethod(method.name())
                            .count(count)
                            .totalAmount(totalAmount)
                            .percentage(BigDecimal.valueOf(percentage)
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue())
                            .build();
                })
                .collect(Collectors.toList());

        return ContributionByPaymentMethod.builder()
                .paymentMethods(paymentMethods)
                .build();
    }

    @Override
    @Cacheable(value = "topEmployers", key = "#limit", unless = "#result == null")
    public TopEmployersResponse getTopEmployers(int limit) {
        log.info("Generating top {} employers", limit);

        List<Employer> topEmployers = employerRepository.findTopEmployersByMemberCount();

        List<TopEmployersResponse.EmployerData> employerDataList = topEmployers.stream()
                .limit(limit)
                .map(employer -> {
                    Long memberCount = (long) employer.getMembers().size();

                    // Calculate total contributions for this employer's members
                    BigDecimal totalContributions = employer.getMembers().stream()
                            .map(member -> {
                                BigDecimal amount = contributionRepository.getTotalContributionsByMember(member);
                                return amount != null ? amount : BigDecimal.ZERO;
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return TopEmployersResponse.EmployerData.builder()
                            .employerId(employer.getEmployerId())
                            .companyName(employer.getCompanyName())
                            .memberCount(memberCount)
                            .totalContributions(totalContributions)
                            .build();
                })
                .collect(Collectors.toList());

        return TopEmployersResponse.builder()
                .topEmployers(employerDataList)
                .build();
    }

    @Override
    @Cacheable(value = "recentActivity", key = "#limit", unless = "#result == null")
    public RecentActivityResponse getRecentActivity(int limit) {
        log.info("Generating recent activity (limit: {})", limit);

        // Get recent contributions (most recent first)
        List<Contribution> recentContributions = contributionRepository
                .findAll()
                .stream()
                .sorted((c1, c2) -> c2.getContributionDate().compareTo(c1.getContributionDate()))
                .limit(limit)
                .collect(Collectors.toList());

        // Convert to activity items
        List<RecentActivityResponse.ActivityItem> activities = recentContributions.stream()
                .map(contribution -> {
                    String memberName = contribution.getMember() != null
                            ? contribution.getMember().getFirstName() + " " + contribution.getMember().getLastName()
                            : "Unknown";

                    String contributionTypeStr = contribution.getContributionType() != null
                            ? contribution.getContributionType().toString().toLowerCase()
                            : "unknown";

                    String description = String.format("%s made a %s contribution",
                            memberName,
                            contributionTypeStr);

                    return RecentActivityResponse.ActivityItem.builder()
                            .activityType("CONTRIBUTION")
                            .description(description)
                            .amount(contribution.getContributionAmount())
                            .timestamp(contribution.getContributionDate())
                            .entityId(contribution.getId() != null ? contribution.getId().toString() : null)
                            .entityName(memberName)
                            .build();
                })
                .collect(Collectors.toList());

        return RecentActivityResponse.builder()
                .recentActivities(activities)
                .build();
    }
}
