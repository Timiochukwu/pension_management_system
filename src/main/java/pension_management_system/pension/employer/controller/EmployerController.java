package pension_management_system.pension.employer.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.common.dto.ApiResponseDto;
import pension_management_system.pension.dto.EmployerRequest;
import pension_management_system.pension.dto.EmployerResponse;
import pension_management_system.pension.employer.service.EmployerService;

import java.time.LocalDateTime;
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



}
