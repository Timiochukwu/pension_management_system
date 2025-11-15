package pension_management_system.pension.verification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.verification.dto.BvnVerificationRequest;
import pension_management_system.pension.verification.dto.BvnVerificationResponse;
import pension_management_system.pension.verification.service.BvnVerificationService;

/**
 * BvnVerificationController - BVN verification endpoints
 *
 * Endpoints:
 * - Verify BVN
 * - Get verification status
 */
@RestController
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
@Tag(name = "BVN Verification", description = "Bank Verification Number (BVN) verification for Nigerian market compliance")
public class BvnVerificationController {

    private final BvnVerificationService bvnVerificationService;

    @Operation(summary = "Verify BVN", description = "Verify member identity using BVN (Nigerian regulatory requirement)")
    @PostMapping("/bvn/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MEMBER')")
    public ResponseEntity<BvnVerificationResponse> verifyBvn(
            @PathVariable Long memberId,
            @Valid @RequestBody BvnVerificationRequest request
    ) {
        BvnVerificationResponse response = bvnVerificationService.verifyBvn(memberId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get verification status", description = "Get BVN verification status for member")
    @GetMapping("/bvn/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MEMBER')")
    public ResponseEntity<BvnVerificationResponse> getVerificationStatus(@PathVariable Long memberId) {
        BvnVerificationResponse response = bvnVerificationService.getVerificationStatus(memberId);
        return ResponseEntity.ok(response);
    }
}
