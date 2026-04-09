package com.office.monitoring.eventTimeline;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EventTimelineDto {

    private String date;
    private RiskSummary riskSummary;
    private DaySummary daySummary;
    private List<EventItem> events;

    @Getter
    @Builder
    public static class RiskSummary {
        private int totalScore;       // 7일 위험 지수
        private String riskLevel;     // 안전 / 주의 / 위험
        private String dateRange;     // "3/19 ~ 3/25"
    }

    @Getter
    @Builder
    public static class DaySummary {
        private int totalCount;
        private int fallCount;
        private int noMotionCount;
        private int violentCount;
    }

    @Getter
    @Builder
    public static class EventItem {
        private Long id;
        private String eventType;
        private String timestamp;     // "07:22"
        private String date;          // "2026-03-25"
        private String status;        // 신고완료(가족신고) 등
        private String imagePath;     // 첫 번째 이미지
        private LocalDateTime lastMotionTimestamp;
        private Integer personCount;
        private Double maxVelocity;
    }
}
