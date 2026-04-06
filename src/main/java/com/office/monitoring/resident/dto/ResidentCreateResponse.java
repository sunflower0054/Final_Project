package com.office.monitoring.resident.dto;

/** ResidentCreateResponse 타입을 정의한다. */
public record ResidentCreateResponse(
        boolean success,
        Long residentId,
        String message
) {
}
