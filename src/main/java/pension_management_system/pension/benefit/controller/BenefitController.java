package pension_management_system.pension.benefit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.benefit.dto.BenefitCalculationResponse;
import pension_management_system.pension.benefit.dto.BenefitRequest;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.entity.BenefitType;
import pension_management_system.pension.benefit.service.BenefitService;
import pension_management_system.pension.common.dto.ApiResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/v1/benefits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Benefits", description = "Pension Benefit Management APIs")
public class BenefitController {

    private final BenefitService benefitService;

    @GetMapping("/calculate/{memberId}")
    @Operation(summary = "Calculate estimated benefit for a member")
    public ResponseEntity<ApiResponseDto<BenefitCalculationResponse>> calculateBenefit(
            @PathVariable Long memberId,
            @RequestParam BenefitType benefitType) {
        log.info("GET /api/v1/benefits/calculate/{} - Calculate {} benefit", memberId, benefitType);
        BenefitCalculationResponse calculation = benefitService.calculateBenefit(memberId, benefitType);
        ApiResponseDto<BenefitCalculationResponse> response = ApiResponseDto.<BenefitCalculationResponse>builder()
                .success(true)
                .message("Benefit calculation completed successfully")
                .data(calculation)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Apply for a pension benefit")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> applyForBenefit(
            @Valid @RequestBody BenefitRequest request) {
        log.info("POST /api/v1/benefits - Apply for benefit");
        BenefitResponse benefit = benefitService.applyForBenefit(request);
        ApiResponseDto<BenefitResponse> response = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit application submitted successfully")
                .data(benefit)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get benefit by ID")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> getBenefitById(@PathVariable Long id) {
        log.info("GET /api/v1/benefits/{} - Get benefit by ID", id);
        BenefitResponse benefit = benefitService.getBenefitById(id);
        ApiResponseDto<BenefitResponse> response = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit retrieved successfully")
                .data(benefit)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Get benefit by reference number")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> getBenefitByReference(
            @PathVariable String referenceNumber) {
        log.info("GET /api/v1/benefits/reference/{} - Get benefit by reference", referenceNumber);
        BenefitResponse benefit = benefitService.getBenefitByReference(referenceNumber);
        ApiResponseDto<BenefitResponse> response = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit retrieved successfully")
                .data(benefit)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get all benefits for a member")
    public ResponseEntity<ApiResponseDto<List<BenefitResponse>>> getMemberBenefits(
            @PathVariable Long memberId) {
        log.info("GET /api/v1/benefits/member/{} - Get member benefits", memberId);
        List<BenefitResponse> benefits = benefitService.getMemberBenefits(memberId);
        ApiResponseDto<List<BenefitResponse>> response = ApiResponseDto.<List<BenefitResponse>>builder()
                .success(true)
                .message("Member benefits retrieved successfully")
                .data(benefits)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get benefits by status")
    public ResponseEntity<ApiResponseDto<List<BenefitResponse>>> getBenefitsByStatus(
            @PathVariable BenefitStatus status) {
        log.info("GET /api/v1/benefits/status/{} - Get benefits by status", status);
        List<BenefitResponse> benefits = benefitService.getBenefitsByStatus(status);
        ApiResponseDto<List<BenefitResponse>> response = ApiResponseDto.<List<BenefitResponse>>builder()
                .success(true)
                .message("Benefits retrieved successfully")
                .data(benefits)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a benefit application")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> approveBenefit(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        log.info("PUT /api/v1/benefits/{}/approve - Approve benefit by {}", id, approvedBy);
        BenefitResponse benefit = benefitService.approveBenefit(id, approvedBy);
        ApiResponseDto<BenefitResponse> response = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit approved successfully")
                .data(benefit)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a benefit application")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> rejectBenefit(
            @PathVariable Long id,
            @RequestParam String reason) {
        log.info("PUT /api/v1/benefits/{}/reject - Reject benefit", id);
        BenefitResponse benefit = benefitService.rejectBenefit(id, reason);
        ApiResponseDto<BenefitResponse> response = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit rejected successfully")
                .data(benefit)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/disburse")
    @Operation(summary = "Disburse an approved benefit")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> disburseBenefit(
            @PathVariable Long id,
            @RequestParam String disbursedBy) {
        log.info("PUT /api/v1/benefits/{}/disburse - Disburse benefit by {}", id, disbursedBy);
        BenefitResponse benefit = benefitService.disburseBenefit(id, disbursedBy);
        ApiResponseDto<BenefitResponse> response = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit disbursed successfully")
                .data(benefit)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a benefit application")
    public ResponseEntity<ApiResponseDto<Void>> cancelBenefit(@PathVariable Long id) {
        log.info("DELETE /api/v1/benefits/{} - Cancel benefit", id);
        benefitService.cancelBenefit(id);
        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .success(true)
                .message("Benefit cancelled successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/pending/count")
    @Operation(summary = "Get count of pending benefits")
    public ResponseEntity<ApiResponseDto<Long>> countPendingBenefits() {
        log.info("GET /api/v1/benefits/analytics/pending/count - Count pending benefits");
        long count = benefitService.countPendingBenefits();
        ApiResponseDto<Long> response = ApiResponseDto.<Long>builder()
                .success(true)
                .message("Pending benefits count retrieved successfully")
                .data(count)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/member/{memberId}/pending/count")
    @Operation(summary = "Get count of pending benefits for a member")
    public ResponseEntity<ApiResponseDto<Long>> countMemberPendingBenefits(@PathVariable Long memberId) {
        log.info("GET /api/v1/benefits/analytics/member/{}/pending/count - Count member pending benefits", memberId);
        long count = benefitService.countMemberPendingBenefits(memberId);
        ApiResponseDto<Long> response = ApiResponseDto.<Long>builder()
                .success(true)
                .message("Member pending benefits count retrieved successfully")
                .data(count)
                .build();
        return ResponseEntity.ok(response);
    }
}
