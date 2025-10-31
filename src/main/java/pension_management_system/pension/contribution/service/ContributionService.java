package pension_management_system.pension.contribution.service;

import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.entity.Contribution;

public interface ContributionService {
    ContributionResponse processContribution(ContributionRequest request);

}
