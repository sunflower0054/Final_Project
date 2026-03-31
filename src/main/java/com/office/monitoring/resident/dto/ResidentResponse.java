package com.office.monitoring.resident.dto;

import com.office.monitoring.resident.Resident;

import java.time.LocalDate;

public record ResidentResponse(
        Long id,
        String name,
        LocalDate birthDate,
        String address,
        String phone,
        String disease,
        Double latitude,
        Double longitude
) {
    public static ResidentResponse from(Resident resident) {
        return new ResidentResponse(
                resident.getId(),
                resident.getName(),
                resident.getBirthDate(),
                resident.getAddress(),
                resident.getPhone(),
                resident.getDisease(),
                resident.getLatitude(),
                resident.getLongitude()
        );
    }
}
