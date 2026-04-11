package com.office.monitoring.resident.dto;

import com.office.monitoring.resident.Resident;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 거주자 데이터 조회·등록·수정·삭제 흐름을 담당하는 구성 요소. */
public record ResidentResponse(
        Long id,
        String name,
        LocalDate birthDate,
        String address,
        String phone,
        String disease,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt
) {
    /** 요청/엔티티 데이터를 다른 표현 객체로 변환해 반환한다. */
    public static ResidentResponse from(Resident resident) {
        return new ResidentResponse(
                resident.getId(),
                resident.getName(),
                resident.getBirthDate(),
                resident.getAddress(),
                resident.getPhone(),
                resident.getDisease(),
                resident.getLatitude(),
                resident.getLongitude(),
                resident.getCreatedAt()
        );
    }
}
