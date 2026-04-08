package com.office.monitoring.admin;

import java.util.List;

/** 관리자 통계 API 응답 구조를 모아둔 DTO. */
public final class AdminStatsDTO {

    private AdminStatsDTO() {
    }

    public record UserStatsResponse(
            boolean success,
            long totalUsers,
            List<List<Object>> ageGroups,
            List<List<Object>> purposes
    ) {
    }

    public record ResidentStatsResponse(
            boolean success,
            long totalResidents,
            double averageAge,
            List<List<Object>> ageGroups
    ) {
    }

    public record EventStatsResponse(
            boolean success,
            long totalEvents,
            List<List<Object>> byType,
            List<List<Object>> byStatus,
            List<List<Object>> monthlyTrend
    ) {
    }
}
