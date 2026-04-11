package com.office.monitoring.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDto {

    private boolean triggered;          // 알림 발생 여부
    private String fallDate;            // 낙상 발생 날짜 "2026-04-05"
    private String fallTime;            // 낙상 발생 시각 "14:30"
    private double avgBefore;           // 낙상 이전 7일 평균
    private double avgAfter;            // 낙상 이후 3일 평균
    private int dropPercent;            // 감소율 (%)
    private String message;             // 표시할 메시지
}