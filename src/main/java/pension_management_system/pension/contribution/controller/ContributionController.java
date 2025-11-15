package pension_management_system.pension.contribution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.common.dto.ApiResponseDto;
import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.dto.ContributionStatementResponse;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.service.ContributionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contributions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contributions",description = "Contribution Management System APIs")
public class ContributionController {
    private final ContributionService contributionService;

    @PostMapping
    @Operation(summary = "process new contribution")
    public ResponseEntity<ApiResponseDto<ContributionResponse>> processNewContribution(@Valid @RequestBody ContributionRequest request) {
        log.info("POST /api/v1/contributions - Process new contribution");
        ContributionResponse response = contributionService.processContribution(request);
        ApiResponseDto<ContributionResponse> apiResponseDto = ApiResponseDto.<ContributionResponse>builder()
                .success(true)
                .message("Contribution process successfully")
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseDto);
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get all contributions for a member")
    public ResponseEntity<ApiResponseDto<List<ContributionResponse>>> getMemberContributions(@PathVariable Long memberId) {
        log.info("GET /api/v1/contributions/member/{} - Get member contributions", memberId);
        List<ContributionResponse> contributions = contributionService.getMemberContributions(memberId);
        ApiResponseDto<List<ContributionResponse>> response = ApiResponseDto.<List<ContributionResponse>>builder()
                .success(true)
                .message("Contributions retrieved successfully")
                .data(contributions)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contribution by ID")
    public ResponseEntity<ApiResponseDto<ContributionResponse>> getContributionById(@PathVariable Long id) {
        log.info("GET /api/v1/contributions/{} - Get contribution by ID", id);
        ContributionResponse contribution = contributionService.getContributionById(id);
        ApiResponseDto<ContributionResponse> response = ApiResponseDto.<ContributionResponse>builder()
                .success(true)
                .message("Contribution retrieved successfully")
                .data(contribution)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}/total")
    @Operation(summary = "Calculate total contributions for a member")
    public ResponseEntity<ApiResponseDto<BigDecimal>> getTotalContributions(@PathVariable Long memberId) {
        log.info("GET /api/v1/contributions/member/{}/total - Calculate total contributions", memberId);
        BigDecimal total = contributionService.calculateTotalContributions(memberId);
        ApiResponseDto<BigDecimal> response = ApiResponseDto.<BigDecimal>builder()
                .success(true)
                .message("Total contributions calculated successfully")
                .data(total)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}/total/{type}")
    @Operation(summary = "Calculate total contributions by type for a member")
    public ResponseEntity<ApiResponseDto<BigDecimal>> getTotalByType(
            @PathVariable Long memberId,
            @PathVariable ContributionType type) {
        log.info("GET /api/v1/contributions/member/{}/total/{} - Calculate total by type", memberId, type);
        BigDecimal total = contributionService.calculateTotalByType(memberId, type);
        ApiResponseDto<BigDecimal> response = ApiResponseDto.<BigDecimal>builder()
                .success(true)
                .message("Total " + type + " contributions calculated successfully")
                .data(total)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}/statement")
    @Operation(summary = "Generate contribution statement for a member")
    public ResponseEntity<ApiResponseDto<ContributionStatementResponse>> generateStatement(
            @PathVariable Long memberId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("GET /api/v1/contributions/member/{}/statement - Generate statement from {} to {}",
                memberId, startDate, endDate);
        ContributionStatementResponse statement = contributionService.generateStatement(memberId, startDate, endDate);
        ApiResponseDto<ContributionStatementResponse> response = ApiResponseDto.<ContributionStatementResponse>builder()
                .success(true)
                .message("Statement generated successfully")
                .data(statement)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}/period")
    @Operation(summary = "Get contributions for a member within a date range")
    public ResponseEntity<ApiResponseDto<List<ContributionResponse>>> getContributionsByPeriod(
            @PathVariable Long memberId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("GET /api/v1/contributions/member/{}/period - Get contributions from {} to {}",
                memberId, startDate, endDate);
        List<ContributionResponse> contributions = contributionService.getContributionsByPeriod(memberId, startDate, endDate);
        ApiResponseDto<List<ContributionResponse>> response = ApiResponseDto.<List<ContributionResponse>>builder()
                .success(true)
                .message("Contributions retrieved successfully")
                .data(contributions)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing contribution")
    public ResponseEntity<ApiResponseDto<ContributionResponse>> updateContribution(
            @PathVariable Long id,
            @Valid @RequestBody ContributionRequest request) {
        log.info("PUT /api/v1/contributions/{} - Update contribution", id);
        ContributionResponse response = contributionService.updateContribution(id, request);
        ApiResponseDto<ContributionResponse> apiResponse = ApiResponseDto.<ContributionResponse>builder()
                .success(true)
                .message("Contribution updated successfully")
                .data(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contribution")
    public ResponseEntity<ApiResponseDto<Void>> deleteContribution(@PathVariable Long id) {
        log.info("DELETE /api/v1/contributions/{} - Delete contribution", id);
        contributionService.deleteContribution(id);
        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .success(true)
                .message("Contribution deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
