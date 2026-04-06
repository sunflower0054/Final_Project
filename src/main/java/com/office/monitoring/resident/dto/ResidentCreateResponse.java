package com.office.monitoring.resident.dto;

public record ResidentCreateResponse(
        boolean success,
        Long residentId,
        String message
) {
}
