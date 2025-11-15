package pension_management_system.pension.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pension_management_system.pension.analytics.dto.MemberAnalyticsDto;
import pension_management_system.pension.analytics.dto.SystemStatisticsDto;
import pension_management_system.pension.analytics.service.AnalyticsService;
import pension_management_system.pension.common.dto.ApiResponseDto;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Reporting and Analytics APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/system/statistics")
    @Operation(summary = "Get system-wide statistics")
    public ResponseEntity<ApiResponseDto<SystemStatisticsDto>> getSystemStatistics() {
        log.info("GET /api/v1/analytics/system/statistics - Get system statistics");
        SystemStatisticsDto statistics = analyticsService.getSystemStatistics();
        ApiResponseDto<SystemStatisticsDto> response = ApiResponseDto.<SystemStatisticsDto>builder()
                .success(true)
                .message("System statistics retrieved successfully")
                .data(statistics)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get analytics for a specific member")
    public ResponseEntity<ApiResponseDto<MemberAnalyticsDto>> getMemberAnalytics(@PathVariable Long memberId) {
        log.info("GET /api/v1/analytics/member/{} - Get member analytics", memberId);
        MemberAnalyticsDto analytics = analyticsService.getMemberAnalytics(memberId);
        ApiResponseDto<MemberAnalyticsDto> response = ApiResponseDto.<MemberAnalyticsDto>builder()
                .success(true)
                .message("Member analytics retrieved successfully")
                .data(analytics)
                .build();
        return ResponseEntity.ok(response);
    }
}
