package pension_management_system.pension.benefit.service;

import pension_management_system.pension.benefit.dto.BenefitCalculationResponse;
import pension_management_system.pension.benefit.dto.BenefitRequest;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.entity.BenefitType;

import java.util.List;

public interface BenefitService {

    // Benefit application and calculation
    BenefitCalculationResponse calculateBenefit(Long memberId, BenefitType benefitType);
    BenefitResponse applyForBenefit(BenefitRequest request);

    // Benefit retrieval
    BenefitResponse getBenefitById(Long id);
    BenefitResponse getBenefitByReference(String referenceNumber);
    List<BenefitResponse> getMemberBenefits(Long memberId);
    List<BenefitResponse> getBenefitsByStatus(BenefitStatus status);

    // Benefit processing
    BenefitResponse approveBenefit(Long id, String approvedBy);
    BenefitResponse rejectBenefit(Long id, String reason);
    BenefitResponse disburseBenefit(Long id, String disbursedBy);
    void cancelBenefit(Long id);

    // Analytics
    long countPendingBenefits();
    long countMemberPendingBenefits(Long memberId);
}
