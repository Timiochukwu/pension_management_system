package pension_management_system.pension.analytics.service;

import pension_management_system.pension.analytics.dto.*;

public interface AnalyticsService {
    DashboardStatisticsResponse getDashboardStatistics();
    ContributionTrendResponse getContributionTrend(int months);
    MemberStatusDistribution getMemberStatusDistribution();
    ContributionByPaymentMethod getContributionByPaymentMethod();
    TopEmployersResponse getTopEmployers(int limit);
}
