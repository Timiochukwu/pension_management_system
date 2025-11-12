package pension_management_system.pension.contribution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pension_management_system.pension.common.dto.ApiResponseDto;
import pension_management_system.pension.contribution.dto.ContributionRequest;
import pension_management_system.pension.contribution.dto.ContributionResponse;
import pension_management_system.pension.contribution.service.ContributionService;

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
}
