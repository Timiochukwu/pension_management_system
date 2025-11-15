package pension_management_system.pension.employer.controller;


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
import pension_management_system.pension.common.dto.ApiResponseDto;
import pension_management_system.pension.employer.dto.EmployerRequest;
import pension_management_system.pension.employer.dto.EmployerResponse;
import pension_management_system.pension.employer.service.EmployerService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employers", description = "Employer management APIs")
public class EmployerController {
    private final EmployerService employerService;
    /*
    * @param request Member data from request body
     * @return ResponseEntity with created member and 201 status
     */
    @PostMapping
    @Operation(summary = "Register an employer")
    public ResponseEntity<ApiResponseDto<EmployerResponse>> registerEmployer(@Valid @RequestBody EmployerRequest request){
        log.info("POST /api/v1/employers - Register employer: {}", request.getCompanyName());

        EmployerResponse response = employerService.registerEmployer(request);
        ApiResponseDto<EmployerResponse> apiResponseDto = ApiResponseDto.<EmployerResponse>builder()
                .success(true)
                .message("Employer Register successfully")
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseDto);
    }
    @PutMapping("/{id}")
    @Operation(summary = "Uodate an employer details")
    public ResponseEntity<ApiResponseDto<EmployerResponse>> updateEmployer(@Valid @RequestBody EmployerRequest request, @PathVariable Long id){
        log.info("PUT /api/v1/employers - Update employer: {}", request.getCompanyName());

        EmployerResponse response = employerService.updateEmployer(id, request);
        ApiResponseDto<EmployerResponse> apiResponseDto = ApiResponseDto.<EmployerResponse>builder()
                .success(true)
                .message("Employer Updated Succesfully")
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get an employer details with id")
    public ResponseEntity<ApiResponseDto<EmployerResponse>> getEmployer(@PathVariable Long id){
        log.info("GET /api/v1/employers - Get employer: {}", id);

        EmployerResponse response = employerService.getEmployerById(id);
        ApiResponseDto<EmployerResponse> apiResponseDto = ApiResponseDto.<EmployerResponse>builder()
                .success(true)
                .message("Success fetch Employer")
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @GetMapping("/{employerId:[A-Za-z]{3}\\d+}")
    @Operation(summary = "Get an employer details with employerId")
    public ResponseEntity<ApiResponseDto<EmployerResponse>> getEmployerById(@PathVariable String employerId){
        log.info("GET /api/v1/employers - get employer: {}", employerId);

        EmployerResponse response = employerService.getEmployerByEmployerId(employerId);
        ApiResponseDto<EmployerResponse> apiResponseDto = ApiResponseDto.<EmployerResponse>builder()
                .success(true)
                .message("Success fetch Employer")
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }
    @GetMapping
    @Operation(summary = "Get all active employer ")
    public ResponseEntity<ApiResponseDto<List<EmployerResponse>>> getAllEmployer(){
        log.info("GET /api/v1/employers");
        List<EmployerResponse> responses = employerService.getAllActiveEmployers();
        ApiResponseDto<List<EmployerResponse>> apiResponseDto = ApiResponseDto.<List<EmployerResponse>>builder()
                .success(true)
                .message("Success fetch all active employer")
                .data(responses)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }
//    @GetMapping
//    @Operation(summary = "Get all active employer ")
//    public ResponseEntity<ApiResponseDto<List<EmployerResponse>>> getAllActiveEmployer(){
//        log.info("GET /api/v1/employers");
//        List<EmployerResponse> responses = employerService.get
//    }
@DeleteMapping("/{id}")
@Operation(summary = "delete an employer ")
public ResponseEntity<ApiResponseDto<Void>> deleteEmployer(@PathVariable Long id){
    log.info("DELETE /api/v1/employers - delete employer: {}", id);
    employerService.softDeleteEmployer(id);
    ApiResponseDto<Void> apiResponseDto = ApiResponseDto.<Void>builder()
            .success(true)
            .message("Delete Employer Successfully")
            .build();
    return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);

}
    @PutMapping("/activate/{id}")
    @Operation(summary = "delete an employer ")
    public ResponseEntity<ApiResponseDto<Void>> activateEmployer(@PathVariable Long id){
        log.info("DELETE /api/v1/employers - delete employer: {}", id);
        employerService.reactivateEmployer(id);
        ApiResponseDto<Void> apiResponseDto = ApiResponseDto.<Void>builder()
                .success(true)
                .message(" Employer Activated Successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);

    }
    @PutMapping("/deactivate/{id}")
    @Operation(summary = "delete an employer ")
    public ResponseEntity<ApiResponseDto<Void>> deactivateEmployer(@PathVariable Long id){
        log.info("DELETE /api/v1/employers - delete employer: {}", id);
        employerService.deactivateEmployer(id);
        ApiResponseDto<Void> apiResponseDto = ApiResponseDto.<Void>builder()
                .success(true)
                .message(" Employer Deactivated Successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);

    }

    /**
     * ADVANCED SEARCH ENDPOINT
     *
     * Purpose: Search employers with multiple filter criteria
     * URL: GET /api/v1/employers/search
     *
     * How to use (example URLs):
     * - Search by company name: /api/v1/employers/search?companyName=Tech&page=0&size=10
     * - Search by city: /api/v1/employers/search?city=Lagos
     * - Multiple filters: /api/v1/employers/search?companyName=Tech&city=Lagos&active=true
     * - With sorting: /api/v1/employers/search?city=Lagos&sortBy=companyName&sortDirection=ASC
     *
     * Pagination parameters:
     * - page: Page number (starts from 0)
     * - size: Number of results per page (default 10)
     * - sortBy: Field to sort by (e.g., "companyName", "createdAt")
     * - sortDirection: ASC (ascending) or DESC (descending)
     *
     * All filter parameters are optional (@RequestParam(required = false))
     * If no filters provided, returns all employers (paginated)
     */
    @GetMapping("/search")
    @Operation(summary = "Search and filter employers", description = "Advanced search with multiple filter criteria and pagination")
    public ResponseEntity<ApiResponseDto<Page<EmployerResponse>>> searchEmployers(
            @RequestParam(required = false) String employerId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String registrationNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        // Log the incoming request for debugging
        log.info("Searching employers with filters - page: {}, size: {}", page, size);

        // STEP 1: Determine sort direction (ASC or DESC)
        // equalsIgnoreCase makes it case-insensitive ("desc", "DESC", "Desc" all work)
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // STEP 2: Create Pageable object
        // This tells Spring Data JPA which page to fetch and how to sort
        // Example: PageRequest.of(0, 10, Sort.by(ASC, "companyName"))
        //          means: "Give me first page, 10 items, sorted by company name ascending"
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // STEP 3: Call service to execute search
        // Service builds dynamic query and returns paginated results
        Page<EmployerResponse> employers = employerService.searchEmployers(
                employerId, companyName, registrationNumber, email, phoneNumber,
                industry, active, city, state, country, pageable
        );

        // STEP 4: Wrap result in standard API response format
        ApiResponseDto<Page<EmployerResponse>> apiResponseDto = ApiResponseDto.<Page<EmployerResponse>>builder()
                .success(true)
                .message("Employers search completed successfully")
                .data(employers)  // Contains: content (list of employers), totalPages, totalElements, etc.
                .build();

        // STEP 5: Return HTTP 200 OK with search results
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    /**
     * QUICK SEARCH ENDPOINT
     *
     * Purpose: Simple one-keyword search across multiple fields
     * URL: GET /api/v1/employers/quick-search
     *
     * How to use:
     * - /api/v1/employers/quick-search?searchTerm=microsoft
     * - /api/v1/employers/quick-search?searchTerm=tech&page=0&size=20
     *
     * What it searches:
     * - Company name
     * - Employer ID
     * - Email
     * - Registration number
     * - Industry
     * - Phone number
     *
     * Perfect for: Search boxes where user types one term
     */
    @GetMapping("/quick-search")
    @Operation(summary = "Quick search employers", description = "Search employers by keyword across multiple fields")
    public ResponseEntity<ApiResponseDto<Page<EmployerResponse>>> quickSearch(
            @RequestParam String searchTerm,  // required parameter - must be provided
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.info("Quick search for employers with term: {}", searchTerm);

        // Determine sort direction
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Create pagination settings
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Execute quick search through service
        Page<EmployerResponse> employers = employerService.quickSearch(searchTerm, pageable);

        // Wrap and return results
        ApiResponseDto<Page<EmployerResponse>> apiResponseDto = ApiResponseDto.<Page<EmployerResponse>>builder()
                .success(true)
                .message("Quick search completed successfully")
                .data(employers)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

}
