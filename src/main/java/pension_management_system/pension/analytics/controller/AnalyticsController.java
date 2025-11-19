package pension_management_system.pension.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pension_management_system.pension.analytics.dto.*;
import pension_management_system.pension.analytics.service.AnalyticsService;
import pension_management_system.pension.common.dto.ApiResponseDto;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Analytics and Dashboard APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get dashboard statistics", description = "Get overall statistics for the dashboard")
    @GetMapping("/dashboard/statistics")
    public ResponseEntity<ApiResponseDto<DashboardStatisticsResponse>> getDashboardStatistics() {
        log.info("Received request for dashboard statistics");
        DashboardStatisticsResponse statistics = analyticsService.getDashboardStatistics();

        ApiResponseDto<DashboardStatisticsResponse> response = ApiResponseDto.<DashboardStatisticsResponse>builder()
                .success(true)
                .message("Dashboard statistics retrieved successfully")
                .data(statistics)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get dashboard (alias)", description = "Get overall statistics for the dashboard (alias endpoint)")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDto<DashboardStatisticsResponse>> getDashboard() {
        return getDashboardStatistics();
    }

    @Operation(summary = "Get contribution trend", description = "Get contribution trend for specified number of months")
    @GetMapping("/contributions/trend")
    public ResponseEntity<ApiResponseDto<ContributionTrendResponse>> getContributionTrend(
            @RequestParam(defaultValue = "12") int months) {
        log.info("Received request for contribution trend for {} months", months);

        if (months < 1 || months > 24) {
            ApiResponseDto<ContributionTrendResponse> response = ApiResponseDto.<ContributionTrendResponse>builder()
                    .success(false)
                    .message("Months must be between 1 and 24")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ContributionTrendResponse trend = analyticsService.getContributionTrend(months);

        ApiResponseDto<ContributionTrendResponse> response = ApiResponseDto.<ContributionTrendResponse>builder()
                .success(true)
                .message("Contribution trend retrieved successfully")
                .data(trend)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get contribution trends (alias)", description = "Get contribution trend for specified number of months (alias endpoint)")
    @GetMapping("/contributions/trends")
    public ResponseEntity<ApiResponseDto<ContributionTrendResponse>> getContributionTrends(
            @RequestParam(defaultValue = "12") int months) {
        return getContributionTrend(months);
    }

    @Operation(summary = "Get member status distribution", description = "Get distribution of members by status")
    @GetMapping("/members/status-distribution")
    public ResponseEntity<ApiResponseDto<MemberStatusDistribution>> getMemberStatusDistribution() {
        log.info("Received request for member status distribution");
        MemberStatusDistribution distribution = analyticsService.getMemberStatusDistribution();

        ApiResponseDto<MemberStatusDistribution> response = ApiResponseDto.<MemberStatusDistribution>builder()
                .success(true)
                .message("Member status distribution retrieved successfully")
                .data(distribution)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get contribution by payment method", description = "Get contribution statistics grouped by payment method")
    @GetMapping("/contributions/by-payment-method")
    public ResponseEntity<ApiResponseDto<ContributionByPaymentMethod>> getContributionByPaymentMethod() {
        log.info("Received request for contribution by payment method");
        ContributionByPaymentMethod data = analyticsService.getContributionByPaymentMethod();

        ApiResponseDto<ContributionByPaymentMethod> response = ApiResponseDto.<ContributionByPaymentMethod>builder()
                .success(true)
                .message("Contribution by payment method retrieved successfully")
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get top employers", description = "Get top employers by member count and contributions")
    @GetMapping("/employers/top")
    public ResponseEntity<ApiResponseDto<TopEmployersResponse>> getTopEmployers(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Received request for top {} employers", limit);

        if (limit < 1 || limit > 50) {
            ApiResponseDto<TopEmployersResponse> response = ApiResponseDto.<TopEmployersResponse>builder()
                    .success(false)
                    .message("Limit must be between 1 and 50")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        TopEmployersResponse topEmployers = analyticsService.getTopEmployers(limit);

        ApiResponseDto<TopEmployersResponse> response = ApiResponseDto.<TopEmployersResponse>builder()
                .success(true)
                .message("Top employers retrieved successfully")
                .data(topEmployers)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get recent activity", description = "Get recent system activities")
    @GetMapping("/recent-activity")
    public ResponseEntity<ApiResponseDto<RecentActivityResponse>> getRecentActivity(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Received request for recent activity (limit: {})", limit);

        if (limit < 1 || limit > 100) {
            ApiResponseDto<RecentActivityResponse> response = ApiResponseDto.<RecentActivityResponse>builder()
                    .success(false)
                    .message("Limit must be between 1 and 100")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        RecentActivityResponse recentActivity = analyticsService.getRecentActivity(limit);

        ApiResponseDto<RecentActivityResponse> response = ApiResponseDto.<RecentActivityResponse>builder()
                .success(true)
                .message("Recent activity retrieved successfully")
                .data(recentActivity)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
