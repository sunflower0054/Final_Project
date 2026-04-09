package com.office.monitoring.admin.dto;

public record AdminEventStatsFilter(
        Integer year,
        Integer month,
        String eventType,
        String status
) {
}
