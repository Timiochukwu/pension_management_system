package pension_management_system.pension.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import pension_management_system.pension.common.dto.ApiResponseDto;
import pension_management_system.pension.member.dto.MemberRequest;
import pension_management_system.pension.member.dto.MemberResponse;
import pension_management_system.pension.member.service.MemberService;
import pension_management_system.pension.benefit.dto.BenefitResponse;
import pension_management_system.pension.benefit.service.BenefitService;

import java.util.List;

import static java.util.stream.DoubleStream.builder;

/**
 * MemberController - REST API endpoints for member management
 * This controller handles HTTP requests for member operations
 * Annotations explained:
 * @RestController - Combination of @Controller + @ResponseBody
 *                   Automatically converts return values to JSON
 * @RequestMapping - Base URL path for all endpoints in this controller
 * @RequiredArgsConstructor - Lombok: Creates constructor for final fields (dependency injection)
 * @Slf4j - Lombok: Provides logging (log.info(), log.error(), etc.)
 * @Tag - Swagger: Groups these endpoints under "Members" in API documentation
 */
@RestController
@RequestMapping("/api/v1/members")  // Base path: http://localhost:8080/api/v1/members
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Members", description = "Member management APIs")
public class MemberController {

    // DEPENDENCY INJECTION
    // Spring automatically injects MemberService implementation
    private final MemberService memberService;
    private final BenefitService benefitService;
    private final RestClient.Builder builder;

    /**
     * CREATE NEW MEMBER
     *
     * HTTP Method: POST
     * URL: POST /api/v1/members
     * Body: JSON with member data
     *
     * Example Request:
     * POST http://localhost:8080/api/v1/members
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "email": "john@example.com",
     *   "phoneNumber": "+2348012345678",
     *   "dateOfBirth": "1990-05-15"
     * }
     *
     * Example Response (201 Created):
     * {
     *   "success": true,
     *   "message": "Member registered successfully",
     *   "data": {
     *     "id": 1,
     *     "memberId": "MEM1699564800000",
     *     "firstName": "John",
     *     "lastName": "Doe",
     *     ...
     *   }
     * }
     *
     * @param request Member data from request body
     * @return ResponseEntity with created member and 201 status
     */
    @PostMapping
    @Operation(summary = "Register a new member", description = "Create a new menber in the pension system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member created succesfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email Already exist")
    })
    public ResponseEntity<ApiResponseDto<MemberResponse>> registerMember(
            @Valid @RequestBody MemberRequest request)
    {
        // Log incoming request (good for debugging)
        log.info("POST /api/v1/members - Register a new member: {}", request.getEmail() );
        // Call service to create member
        MemberResponse response = memberService.registerMember(request);

        //Build success response
        ApiResponseDto<MemberResponse> apiResponse = ApiResponseDto.<MemberResponse>builder()
                .success(true)
                .message("Member registered successfully")
                .data(response)
                .build();
        // Return with HTTP 201 (CREATED) status
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /*
    * @param request Member data from request body
    * * @return ResponseEntity with created member and 201 status
    * */
    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Update a member", description = "Update member in the pension system")
    public ResponseEntity<ApiResponseDto<MemberResponse>> updateMember(
            @PathVariable Long id, @Valid @RequestBody MemberRequest request){
        log.info("PUT /api/v1/members/{}", id);
        MemberResponse response = memberService.updateMember(id, request);
        ApiResponseDto<MemberResponse> apiResponse = ApiResponseDto.<MemberResponse>builder()
                .success(true)
                .message("Member updated successfully")
                .data(response)
                .build();
        // Return with HTTP 200 (CREATED) status
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Fetch a member by database ID", description = "Fetch a member in the pension system")
    public  ResponseEntity<ApiResponseDto<MemberResponse>> getMemberById(@PathVariable Long id){
        log.info("GET /api/v1/members/{}", id);
        MemberResponse response = memberService.getMemberById(id);
        ApiResponseDto<MemberResponse> apiResponseDto = ApiResponseDto.<MemberResponse>builder()
                .success(true)
                .message("Member fetched successfully with database ID")
                .data(response)
                .build();
        // Return with HTTP 200 (OK) Status
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }


    @GetMapping("/{memberId:[A-Za-z]{3}\\d+}")
    @Operation(summary = "Fetch a member by member ID", description = "Fetch a member ID in the pension system")
    public  ResponseEntity<ApiResponseDto<MemberResponse>> getMemberByMemberId(@PathVariable String memberId){
        log.info("GET /api/v1/members/{}", memberId);
        MemberResponse response = memberService.getMemberByMemberId(memberId);
        ApiResponseDto<MemberResponse> apiResponseDto = ApiResponseDto.<MemberResponse>builder()
                .success(true)
                .message("Member fetched successfully with member ID")
                .data(response)
                .build();
        // Return with HTTP 200 (OK) Status
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @GetMapping
    @Operation(summary = "Fetch all active members", description = "Fetch all active members in the pension system with pagination")
    public ResponseEntity<ApiResponseDto<Page<MemberResponse>>> getAllActiveMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.info("GET /api/v1/members - page: {}, size: {}", page, size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<MemberResponse> members = memberService.getAllActiveMembersWithPagination(pageable);
        ApiResponseDto<Page<MemberResponse>> apiResponseDto = ApiResponseDto.<Page<MemberResponse>>builder()
                .success(true)
                .message("All active members fetched successfully")
                .data(members)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }
    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Soft delete a member", description = "Soft delete a member in the pension system")
    public ResponseEntity<ApiResponseDto<Void>> softDeleteMember(@PathVariable Long id){
        log.info("DELETE /api/v1/members/{}", id);
       memberService.sofDeleteMember(id);
       ApiResponseDto<Void> apiResponseDto = ApiResponseDto.<Void>builder()
               .success(true)
               .message("Member deleted successfully")
               .build();
       return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @PutMapping("activate/{id:\\d+}")
    @Operation(summary = "Reactivate a member", description = "Reactivate a deactivated member in the pension system")
    public ResponseEntity<ApiResponseDto<Void>> reactivateMember(@PathVariable Long id){
        log.info("REACTIVATE /api/v1/members/{}", id);
        memberService.reactivateMember(id);
        ApiResponseDto<Void> apiResponseDto = ApiResponseDto.<Void>builder()
                .success(true)
                .message("Member reactivated successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @PutMapping("deactivate/{id:\\d+}")
    @Operation(summary = "Deactivate a member", description = "Deactivate an active member in the pension system")
    public ResponseEntity<ApiResponseDto<Void>> deactivateMember(@PathVariable Long id){
        log.info("Deactivating /api/v1/members/{}", id);
        memberService.deactivateMember(id);
        ApiResponseDto<Void> apiResponseDto = ApiResponseDto.<Void>builder()
                .success(true)
                .message("Member deactivated successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }



    @GetMapping("/claims")
    @Operation(summary = "Get all benefit claims", description = "Get all benefit claims across all members")
    public ResponseEntity<ApiResponseDto<List<BenefitResponse>>> getAllClaims() {
        log.info("GET /api/v1/members/claims - Fetching all benefit claims");
        List<BenefitResponse> claims = benefitService.getAllBenefits();

        ApiResponseDto<List<BenefitResponse>> apiResponseDto = ApiResponseDto.<List<BenefitResponse>>builder()
                .success(true)
                .message("All claims retrieved successfully")
                .data(claims)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

    @GetMapping("/{id:\\d+}/claims")
    @Operation(summary = "Get member's benefit claims", description = "Get all benefit claims for a specific member by database ID")
    public ResponseEntity<ApiResponseDto<List<BenefitResponse>>> getMemberClaims(@PathVariable Long id) {
        log.info("GET /api/v1/members/{}/claims - Fetching benefit claims", id);
        List<BenefitResponse> claims = benefitService.getBenefitsByMemberId(id);

        ApiResponseDto<List<BenefitResponse>> apiResponseDto = ApiResponseDto.<List<BenefitResponse>>builder()
                .success(true)
                .message("Member claims retrieved successfully")
                .data(claims)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponseDto);
    }

}
