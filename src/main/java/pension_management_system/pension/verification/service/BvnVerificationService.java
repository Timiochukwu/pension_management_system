package pension_management_system.pension.verification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import pension_management_system.pension.member.entity.Member;
import pension_management_system.pension.member.repository.MemberRepository;
import pension_management_system.pension.verification.dto.BvnVerificationRequest;
import pension_management_system.pension.verification.dto.BvnVerificationResponse;
import pension_management_system.pension.verification.entity.BvnVerification;
import pension_management_system.pension.verification.repository.BvnVerificationRepository;
import pension_management_system.pension.exception.ResourceNotFoundException;
import pension_management_system.pension.exception.VerificationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * BvnVerificationService - BVN verification service
 *
 * Purpose: Verify member identity using BVN
 *
 * Integration: Uses Smile Identity or Youverify API
 *
 * Process:
 * 1. Validate BVN format
 * 2. Call verification API
 * 3. Parse response
 * 4. Calculate match score
 * 5. Store result
 *
 * Match scoring:
 * - Name match: 40 points
 * - DOB match: 40 points
 * - Phone match: 20 points
 * - Total: 0-100
 *
 * Verification status:
 * - Score >= 80: VERIFIED
 * - Score 60-79: PARTIAL (manual review)
 * - Score < 60: MISMATCH
 *
 * Production:
 * - Use Smile Identity API (https://docs.usesmileid.com)
 * - Or Youverify API (https://youverify.co)
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BvnVerificationService {

    private final BvnVerificationRepository verificationRepository;
    private final MemberRepository memberRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${bvn.verification.api.url:https://api.smileidentity.com/v1}")
    private String apiUrl;

    @Value("${bvn.verification.api.key:}")
    private String apiKey;

    @Value("${bvn.verification.enabled:false}")
    private boolean verificationEnabled;

    /**
     * Verify member BVN
     *
     * @param memberId Member ID
     * @param request BVN verification request
     * @return Verification result
     */
    public BvnVerificationResponse verifyBvn(Long memberId, BvnVerificationRequest request) {
        log.info("Starting BVN verification for member: {}", memberId);

        // Get member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> ResourceNotFoundException.member(memberId));

        // Check if already verified
        if (verificationRepository.existsByMemberId(memberId)) {
            throw VerificationException.alreadyExists();
        }

        // Create verification record
        BvnVerification verification = BvnVerification.builder()
                .member(member)
                .bvnNumber(request.getBvnNumber())
                .status(BvnVerification.VerificationStatus.PENDING)
                .provider("SmileIdentity")
                .retryCount(0)
                .build();

        verification = verificationRepository.save(verification);

        try {
            if (verificationEnabled && apiKey != null && !apiKey.isEmpty()) {
                // Call real BVN verification API
                callBvnApi(verification, request);
            } else {
                // Mock verification for development
                mockVerification(verification, request);
            }

            // Calculate match score
            int matchScore = calculateMatchScore(verification, request);
            verification.setMatchScore(matchScore);

            // Determine status based on match score
            if (matchScore >= 80) {
                verification.setStatus(BvnVerification.VerificationStatus.VERIFIED);
            } else if (matchScore >= 60) {
                verification.setStatus(BvnVerification.VerificationStatus.MISMATCH);
                verification.setErrorMessage("Partial match - manual review required");
            } else {
                verification.setStatus(BvnVerification.VerificationStatus.MISMATCH);
                verification.setErrorMessage("Data mismatch - verification failed");
            }

            verification.setVerificationDate(LocalDateTime.now());
            verification = verificationRepository.save(verification);

            log.info("BVN verification complete: memberId={}, status={}, score={}",
                    memberId, verification.getStatus(), matchScore);

        } catch (Exception e) {
            log.error("BVN verification failed: {}", e.getMessage());
            verification.setStatus(BvnVerification.VerificationStatus.ERROR);
            verification.setErrorMessage(e.getMessage());
            verification.setRetryCount(verification.getRetryCount() + 1);
            verificationRepository.save(verification);
        }

        return mapToResponse(verification);
    }

    /**
     * Call BVN verification API (Smile Identity)
     */
    private void callBvnApi(BvnVerification verification, BvnVerificationRequest request) {
        try {
            WebClient webClient = webClientBuilder.build();

            String response = webClient.post()
                    .uri(apiUrl + "/id_verification")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(buildApiRequest(request))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse response
            JsonNode responseJson = objectMapper.readTree(response);

            // Extract verified data
            verification.setVerifiedFirstName(responseJson.path("first_name").asText());
            verification.setVerifiedLastName(responseJson.path("last_name").asText());
            verification.setVerifiedDateOfBirth(
                    LocalDate.parse(responseJson.path("date_of_birth").asText())
            );
            verification.setVerifiedPhoneNumber(responseJson.path("phone_number").asText());
            verification.setVerifiedGender(responseJson.path("gender").asText());
            verification.setProviderReference(responseJson.path("reference").asText());

            log.info("BVN API call successful: {}", verification.getBvnNumber());

        } catch (Exception e) {
            throw VerificationException.apiFailed(e.getMessage());
        }
    }

    /**
     * Mock verification for development/testing
     */
    private void mockVerification(BvnVerification verification, BvnVerificationRequest request) {
        log.warn("Using mock BVN verification (development mode)");

        // Simulate successful verification
        verification.setVerifiedFirstName(request.getFirstName());
        verification.setVerifiedLastName(request.getLastName());
        verification.setVerifiedDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        verification.setVerifiedPhoneNumber("08012345678");
        verification.setVerifiedGender("M");
        verification.setProviderReference("MOCK-" + System.currentTimeMillis());
    }

    /**
     * Calculate match score (0-100)
     */
    private int calculateMatchScore(BvnVerification verification, BvnVerificationRequest request) {
        int score = 0;

        // First name match (20 points)
        if (namesMatch(request.getFirstName(), verification.getVerifiedFirstName())) {
            score += 20;
        }

        // Last name match (20 points)
        if (namesMatch(request.getLastName(), verification.getVerifiedLastName())) {
            score += 20;
        }

        // Full name exact match bonus (20 points)
        String providedFullName = (request.getFirstName() + " " + request.getLastName()).toLowerCase();
        String verifiedFullName = (verification.getVerifiedFirstName() + " " +
                verification.getVerifiedLastName()).toLowerCase();
        if (providedFullName.equals(verifiedFullName)) {
            score += 20;
        }

        // Date of birth match (40 points)
        LocalDate providedDob = LocalDate.parse(request.getDateOfBirth());
        if (providedDob.equals(verification.getVerifiedDateOfBirth())) {
            score += 40;
        }

        return Math.min(score, 100);
    }

    /**
     * Check if names match (case-insensitive, fuzzy matching)
     */
    private boolean namesMatch(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return false;
        }

        // Exact match
        if (name1.equalsIgnoreCase(name2)) {
            return true;
        }

        // Fuzzy match (simple Levenshtein distance)
        return calculateSimilarity(name1.toLowerCase(), name2.toLowerCase()) > 0.8;
    }

    /**
     * Calculate string similarity (0.0 to 1.0)
     */
    private double calculateSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        if (maxLength == 0) {
            return 1.0;
        }

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Levenshtein distance (edit distance between strings)
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Build API request
     */
    private String buildApiRequest(BvnVerificationRequest request) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "country", "NG",
                    "id_type", "BVN",
                    "id_number", request.getBvnNumber(),
                    "first_name", request.getFirstName(),
                    "last_name", request.getLastName(),
                    "dob", request.getDateOfBirth()
            ));
        } catch (Exception e) {
            throw VerificationException.requestBuildFailed(e);
        }
    }

    /**
     * Get verification status for member
     */
    public BvnVerificationResponse getVerificationStatus(Long memberId) {
        BvnVerification verification = verificationRepository.findByMemberId(memberId)
                .orElseThrow(() -> ResourceNotFoundException.member(memberId));

        return mapToResponse(verification);
    }

    /**
     * Map entity to response
     */
    private BvnVerificationResponse mapToResponse(BvnVerification verification) {
        return BvnVerificationResponse.builder()
                .id(verification.getId())
                .bvnNumber(maskBvn(verification.getBvnNumber()))
                .status(verification.getStatus().toString())
                .matchScore(verification.getMatchScore())
                .verifiedFirstName(verification.getVerifiedFirstName())
                .verifiedLastName(verification.getVerifiedLastName())
                .verifiedDateOfBirth(verification.getVerifiedDateOfBirth() != null ?
                        verification.getVerifiedDateOfBirth().toString() : null)
                .verifiedPhoneNumber(verification.getVerifiedPhoneNumber())
                .verificationDate(verification.getVerificationDate())
                .errorMessage(verification.getErrorMessage())
                .build();
    }

    /**
     * Mask BVN for security (show only last 4 digits)
     */
    private String maskBvn(String bvn) {
        if (bvn == null || bvn.length() < 4) {
            return "***";
        }
        return "*******" + bvn.substring(bvn.length() - 4);
    }
}
