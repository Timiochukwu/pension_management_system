package pension_management_system.pension.benefit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.benefit.dto.BenefitRequest;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.entity.BenefitStatus;
import pension_management_system.pension.benefit.service.BenefitService;
import pension_management_system.pension.common.dto.ApiResponseDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * BenefitController - REST API for benefit management
 *
 * Endpoints:
 * - POST /api/v1/benefits - Create benefit claim
 * - GET /api/v1/benefits/{id} - Get benefit by ID
 * - GET /api/v1/benefits - Get all benefits
 * - GET /api/v1/benefits/member/{memberId} - Get member's benefits
 * - GET /api/v1/benefits/status/{status} - Get benefits by status
 * - PUT /api/v1/benefits/{id} - Update benefit
 * - PUT /api/v1/benefits/{id}/approve - Approve benefit
 * - PUT /api/v1/benefits/{id}/reject - Reject benefit
 * - PUT /api/v1/benefits/{id}/pay - Mark as paid
 * - DELETE /api/v1/benefits/{id} - Delete benefit
 */
@RestController
@RequestMapping("/api/v1/benefits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Benefits", description = "Pension benefit management APIs")
public class BenefitController {

    private final BenefitService benefitService;

    @PostMapping
    @Operation(summary = "Create benefit claim", description = "Submit new benefit claim for a member")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> createBenefit(@Valid @RequestBody BenefitRequest request) {
        log.info("POST /api/v1/benefits - Creating benefit for member: {}", request.getMemberId());
        BenefitResponse response = benefitService.createBenefit(request);

        ApiResponseDto<BenefitResponse> apiResponse = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit claim created successfully")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get benefit by ID")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> getBenefit(@PathVariable Long id) {
        BenefitResponse response = benefitService.getBenefitById(id);

        ApiResponseDto<BenefitResponse> apiResponse = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit retrieved successfully")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @Operation(summary = "Get all benefits")
    public ResponseEntity<ApiResponseDto<List<BenefitResponse>>> getAllBenefits() {
        List<BenefitResponse> benefits = benefitService.getAllBenefits();

        ApiResponseDto<List<BenefitResponse>> apiResponse = ApiResponseDto.<List<BenefitResponse>>builder()
                .success(true)
                .message("Benefits retrieved successfully")
                .data(benefits)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/claims")
    @Operation(summary = "Get all benefit claims with pagination", description = "Retrieve all benefit claims with pagination support")
    public ResponseEntity<ApiResponseDto<Page<BenefitResponse>>> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("GET /api/v1/benefits/claims - page: {}, size: {}", page, size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<BenefitResponse> claims = benefitService.getAllBenefitsWithPagination(pageable);

        ApiResponseDto<Page<BenefitResponse>> apiResponse = ApiResponseDto.<Page<BenefitResponse>>builder()
                .success(true)
                .message("All benefit claims retrieved successfully")
                .data(claims)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/member/{memberId:\\d+}")
    @Operation(summary = "Get benefits by member ID", description = "Get all benefit claims for a specific member")
    public ResponseEntity<ApiResponseDto<List<BenefitResponse>>> getMemberBenefits(@PathVariable Long memberId) {
        List<BenefitResponse> benefits = benefitService.getBenefitsByMemberId(memberId);

        ApiResponseDto<List<BenefitResponse>> apiResponse = ApiResponseDto.<List<BenefitResponse>>builder()
                .success(true)
                .message("Member benefits retrieved successfully")
                .data(benefits)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get benefits by status", description = "Filter benefits by status (PENDING, APPROVED, etc.)")
    public ResponseEntity<ApiResponseDto<List<BenefitResponse>>> getBenefitsByStatus(@PathVariable BenefitStatus status) {
        List<BenefitResponse> benefits = benefitService.getBenefitsByStatus(status);

        ApiResponseDto<List<BenefitResponse>> apiResponse = ApiResponseDto.<List<BenefitResponse>>builder()
                .success(true)
                .message("Benefits retrieved successfully")
                .data(benefits)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Update benefit")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> updateBenefit(
            @PathVariable Long id,
            @Valid @RequestBody BenefitRequest request) {

        BenefitResponse response = benefitService.updateBenefit(id, request);

        ApiResponseDto<BenefitResponse> apiResponse = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit updated successfully")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id:\\d+}/approve")
    @Operation(summary = "Approve benefit", description = "Approve benefit claim and set approved amount")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> approveBenefit(
            @PathVariable Long id,
            @RequestParam BigDecimal approvedAmount,
            @RequestParam String approvedBy) {

        BenefitResponse response = benefitService.approveBenefit(id, approvedAmount, approvedBy);

        ApiResponseDto<BenefitResponse> apiResponse = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit approved successfully")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id:\\d+}/reject")
    @Operation(summary = "Reject benefit", description = "Reject benefit claim with reason")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> rejectBenefit(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam String rejectedBy) {

        BenefitResponse response = benefitService.rejectBenefit(id, reason, rejectedBy);

        ApiResponseDto<BenefitResponse> apiResponse = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit rejected")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id:\\d+}/pay")
    @Operation(summary = "Mark benefit as paid")
    public ResponseEntity<ApiResponseDto<BenefitResponse>> markAsPaid(@PathVariable Long id) {
        BenefitResponse response = benefitService.markAsPaid(id);

        ApiResponseDto<BenefitResponse> apiResponse = ApiResponseDto.<BenefitResponse>builder()
                .success(true)
                .message("Benefit marked as paid")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Delete benefit")
    public ResponseEntity<ApiResponseDto<Void>> deleteBenefit(@PathVariable Long id) {
        benefitService.deleteBenefit(id);

        ApiResponseDto<Void> apiResponse = ApiResponseDto.<Void>builder()
                .success(true)
                .message("Benefit deleted successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
