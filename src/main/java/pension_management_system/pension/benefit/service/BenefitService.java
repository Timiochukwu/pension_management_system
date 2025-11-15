package pension_management_system.pension.benefit.service;

import pension_management_system.pension.benefit.dto.BenefitRequest;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.entity.BenefitStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * BenefitService Interface
 *
 * Purpose: Defines contract for benefit management operations
 * Handles benefit claims from application through payment
 */
public interface BenefitService {

    // CREATE
    BenefitResponse createBenefit(BenefitRequest request);

    // READ
    BenefitResponse getBenefitById(Long id);
    List<BenefitResponse> getAllBenefits();
    List<BenefitResponse> getBenefitsByMemberId(Long memberId);
    List<BenefitResponse> getBenefitsByStatus(BenefitStatus status);

    // UPDATE
    BenefitResponse updateBenefit(Long id, BenefitRequest request);

    // WORKFLOW ACTIONS
    BenefitResponse approveBenefit(Long id, BigDecimal approvedAmount, String approvedBy);
    BenefitResponse rejectBenefit(Long id, String reason, String rejectedBy);
    BenefitResponse markAsPaid(Long id);
    BenefitResponse startReview(Long id);
    BenefitResponse cancelBenefit(Long id);

    // DELETE
    void deleteBenefit(Long id);
}
