package pension_management_system.pension.contribution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.common.dto.ApiResponseDto;
import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.entity.ContributionStatus;
import pension_management_system.pension.contribution.entity.ContributionType;
import pension_management_system.pension.contribution.entity.PaymentMethod;
import pension_management_system.pension.contribution.service.ContributionService;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    /**
     * ADVANCED SEARCH ENDPOINT FOR CONTRIBUTIONS
     *
     * Purpose: Search contributions with multiple filter criteria
     * URL: GET /api/v1/contributions/search
     *
     * How to use (example URLs):
     * - Find by member: /api/v1/contributions/search?memberId=123
     * - Find by status: /api/v1/contributions/search?status=PENDING
     * - Find by date range: /api/v1/contributions/search?contributionDateFrom=2025-01-01&contributionDateTo=2025-12-31
     * - Find by amount range: /api/v1/contributions/search?amountFrom=1000&amountTo=5000
     * - Multiple filters: /api/v1/contributions/search?memberId=123&status=COMPLETED&contributionType=MONTHLY
     *
     * Filter Parameters (all optional):
     * - referenceNumber: Find by reference number (partial match)
     * - memberId: Filter by specific member
     * - contributionType: MONTHLY or VOLUNTARY
     * - status: PENDING, PROCESSING, COMPLETED, FAILED, REVERSED
     * - paymentMethod: BANK_TRANSFER, DIRECT_DEBIT, CHEQUE, CASH, CARD, MOBILE_MONEY
     * - amountFrom: Minimum contribution amount
     * - amountTo: Maximum contribution amount
     * - contributionDateFrom: Start date (format: yyyy-MM-dd)
     * - contributionDateTo: End date (format: yyyy-MM-dd)
     *
     * Pagination Parameters:
     * - page: Page number (starts from 0)
     * - size: Results per page (default 10)
     * - sortBy: Field to sort (e.g., "contributionDate", "contributionAmount")
     * - sortDirection: ASC or DESC
     */
    @GetMapping("/search")
    @Operation(summary = "Search and filter contributions", description = "Advanced search with multiple filter criteria and pagination")
    public ResponseEntity<ApiResponseDto<Page<ContributionResponse>>> searchContributions(
            @RequestParam(required = false) String referenceNumber,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) ContributionType contributionType,
            @RequestParam(required = false) ContributionStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) BigDecimal amountFrom,
            @RequestParam(required = false) BigDecimal amountTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contributionDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contributionDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        // Log incoming request
        log.info("Searching contributions - memberId: {}, status: {}, page: {}, size: {}", memberId, status, page, size);

        // STEP 1: Create sort direction
        // Users can pass "asc" or "desc" in any case
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // STEP 2: Create Pageable object for pagination and sorting
        // Example: Page 0, size 10, sorted by contributionDate descending
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // STEP 3: Execute search through service layer
        // Service builds dynamic SQL based on provided filters
        Page<ContributionResponse> contributions = contributionService.searchContributions(
                referenceNumber, memberId, contributionType, status, paymentMethod,
                amountFrom, amountTo, contributionDateFrom, contributionDateTo, pageable
        );

        // STEP 4: Wrap results in standard API response format
        ApiResponseDto<Page<ContributionResponse>> apiResponseDto = ApiResponseDto.<Page<ContributionResponse>>builder()
                .success(true)
                .message("Contributions search completed successfully")
                .data(contributions)  // Page contains: content, totalElements, totalPages, etc.
                .build();

        // STEP 5: Return HTTP 200 OK with search results
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    /**
     * QUICK SEARCH ENDPOINT FOR CONTRIBUTIONS
     *
     * Purpose: Simple one-keyword search across multiple fields
     * URL: GET /api/v1/contributions/quick-search
     *
     * How to use:
     * - Search by reference: /api/v1/contributions/quick-search?searchTerm=CON2025
     * - Search by member name: /api/v1/contributions/quick-search?searchTerm=John
     * - Search by member email: /api/v1/contributions/quick-search?searchTerm=john@example.com
     *
     * What it searches:
     * - Contribution reference number
     * - Member first name
     * - Member last name
     * - Member email
     *
     * Perfect for: Simple search boxes where user types anything
     */
    @GetMapping("/quick-search")
    @Operation(summary = "Quick search contributions", description = "Search contributions by keyword across multiple fields")
    public ResponseEntity<ApiResponseDto<Page<ContributionResponse>>> quickSearch(
            @RequestParam String searchTerm,  // Required: The keyword to search for
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Quick search for contributions with term: {}", searchTerm);

        // Create sort direction
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Create pagination settings
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Execute quick search (searches across multiple fields with OR)
        Page<ContributionResponse> contributions = contributionService.quickSearch(searchTerm, pageable);

        // Wrap and return results
        ApiResponseDto<Page<ContributionResponse>> apiResponseDto = ApiResponseDto.<Page<ContributionResponse>>builder()
                .success(true)
                .message("Quick search completed successfully")
                .data(contributions)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }
}
