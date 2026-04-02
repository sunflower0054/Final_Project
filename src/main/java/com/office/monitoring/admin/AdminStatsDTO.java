/*
// 1. 본인 프로젝트 경로에 맞게 패키지명을 꼭 넣어주세요. (예: package com.project.dto;)
package com.office.monitoring.admin;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

// 2. 바깥쪽 클래스에는 Getter/Setter가 필요 없습니다.
public class AdminStatsDTO {

    // [AD-01] 회원 통계 응답 DTO
    @Getter
    @Setter // 3. 실제 데이터가 있는 안쪽 클래스에 각각 붙여야 합니다.
    public static class UserStatsResponse {
        private boolean success = true;
        private long totalUsers;
        private List<List<Object>> ageGroups; // 구글 차트용 2차원 배열
        private List<List<Object>> purposes;
    }

    // [AD-02] 독거노인 통계 응답 DTO
    @Getter
    @Setter
    public static class ResidentStatsResponse {
        private boolean success = true;
        private long totalResidents;
        private double averageAge;
        private List<List<Object>> ageGroups;
    }

    // [AD-03] 이벤트 통계 응답 DTO
    @Getter
    @Setter
    public static class EventStatsResponse {
        private boolean success = true;
        private long totalEvents;
        private List<List<Object>> byType;
        private List<List<Object>> byStatus;
        private List<List<Object>> monthlyTrend;
    }
}
*/
