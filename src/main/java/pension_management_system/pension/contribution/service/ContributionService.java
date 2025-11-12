package pension_management_system.pension.contribution.service;

import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.dto.ContributionStatementResponse;
import pension_management_system.pension.contribution.entity.Contribution;
import pension_management_system.pension.contribution.entity.ContributionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ContributionService {
    ContributionResponse processContribution(ContributionRequest request);
    List<ContributionResponse> getMemberContributions(Long memberId);
    ContributionResponse getContributionById(Long id);
    BigDecimal calculateTotalContributions(Long memberId);
    BigDecimal calculateTotalByType(Long memberId, ContributionType type);
    ContributionStatementResponse generateStatement(Long  memberId, LocalDate startDate, LocalDate endDate);
    List<ContributionResponse> getContributionsByPeriod(Long memberId, LocalDate startDate, LocalDate endDate);
}
