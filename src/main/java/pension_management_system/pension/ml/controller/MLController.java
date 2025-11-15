package pension_management_system.pension.ml.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;
import pension_management_system.pension.ml.dto.FraudDetectionRequest;
import pension_management_system.pension.ml.dto.FraudDetectionResponse;
import pension_management_system.pension.ml.dto.RiskAssessmentResponse;
import pension_management_system.pension.ml.service.FraudDetectionService;
import pension_management_system.pension.ml.service.RiskAssessmentService;

/**
 * MLController - Machine Learning API endpoints
 *
 * Provides access to:
 * - Fraud detection
 * - Risk assessment
 * - Anomaly detection
 */
@RestController
@RequestMapping("/api/v1/ml")
@RequiredArgsConstructor
@Tag(name = "Machine Learning", description = "ML-powered fraud detection and risk assessment")
public class MLController {

    private final FraudDetectionService fraudDetectionService;
    private final RiskAssessmentService riskAssessmentService;
    private final MemberRepository memberRepository;

    @Operation(summary = "Detect fraud in transaction", description = "Analyze transaction for fraud using ML model (85%+ accuracy)")
    @PostMapping("/fraud-detection")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<FraudDetectionResponse> detectFraud(@RequestBody FraudDetectionRequest request) {
        FraudDetectionResponse response = fraudDetectionService.detectFraud(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Assess member risk", description = "Calculate member risk score (300-850 scale)")
    @GetMapping("/risk-assessment/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RiskAssessmentResponse> assessRisk(@PathVariable Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        RiskAssessmentResponse response = riskAssessmentService.assessRisk(member);
        return ResponseEntity.ok(response);
    }
}
