package pension_management_system.pension.contribution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
    @Operation(summary = "Get all contribution for s particular member")
    public ResponseEntity<ApiResponseDto<List<ContributionResponse>>> getMemberContributions(@PathVariable Long memberId) {
        log.info("GET /api/v1/contributions/member/{} ", memberId);
        List<ContributionResponse> contribution = contributionService.getMemberContributions(memberId);
        ApiResponseDto<List<ContributionResponse>> apiResponseDto = ApiResponseDto.<List<ContributionResponse>>builder()
                .success(true)
                .message("Contribution retrieved successfully")
                .data(contribution)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @GetMapping("/member/{memberId}/total")
    @Operation(summary = "Calculate total contribution")
    public ResponseEntity<ApiResponseDto<BigDecimal>> getTotalContributions(@PathVariable Long memberId) {
        log.info("GET /api/v1/contributions/member/{} ", memberId);
        BigDecimal total = contributionService.calculateTotalContributions(memberId);
        ApiResponseDto<BigDecimal> apiResponseDto = ApiResponseDto.<BigDecimal>builder()
                .success(true)
                .message("Total contributions calculated")
                .data(total)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @GetMapping("/member/{memberId}/total/{type}")
    @Operation(summary = "Calculate total by type")
    public ResponseEntity<ApiResponseDto<BigDecimal>>  getTotalContributionsByType(@PathVariable Long memberId, @PathVariable ContributionType type) {
        log.info("GET /api/v1/contributions/member/{} ", memberId);

        BigDecimal total = contributionService.calculateTotalByType(memberId, type);
        String message = String.format("Total contributions calculated successfully: %s", total);
        ApiResponseDto<BigDecimal> apiResponseDto = ApiResponseDto.<BigDecimal>builder()
                .success(true)
                .message(message)
                .data(total)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

//    @GetMapping("/member/{memberId}/statement")
//    @Operation(summary = "Generate contribution")
//    public ResponseEntity<ApiResponseDto<ContributionResponse>> getContributionStatement(
//            @PathVariable Long memberId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//        log.info("GET /api/v1/contributions/member/{} ", memberId);
//        ContributionStatementResponse statememt =

}
