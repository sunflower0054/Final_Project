package com.office.monitoring.admin.dto;

import java.util.List;

/** 관리자 통계 API 응답 구조를 모아둔 DTO. */
public final class AdminStatsDTO {

    /** 유틸성 DTO 묶음이라 인스턴스화를 막는다. */
    private AdminStatsDTO() {
    }

    /** 회원 통계 응답을 담는 record. */
    public record UserStatsResponse(
            boolean success,
            long totalUsers,
            List<List<Object>> ageGroups,
            List<List<Object>> purposes
    ) {
    }

    /** 거주자 통계 응답을 담는 record. */
    public record ResidentStatsResponse(
            boolean success,
            long totalResidents,
            double averageAge,
            List<List<Object>> ageGroups
    ) {
    }

    /** 이벤트 통계 응답을 담는 record. */
    public record EventStatsResponse(
            boolean success,
            long totalEvents,
            List<List<Object>> byType,
            List<List<Object>> byStatus,
            List<List<Object>> monthlyTrend
    ) {
    }
}
