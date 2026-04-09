package com.office.monitoring.member.dto;

import com.office.monitoring.member.Member;

public record MyInfoResponse(
        boolean success,
        String username,
        String name,
        String phone,
        Integer birthYear,
        String purpose,
        String role
) {

    public static MyInfoResponse from(Member member) {
        return new MyInfoResponse(
                true,
                member.getUsername(),
                member.getName(),
                member.getPhone(),
                member.getBirthYear(),
                member.getPurpose(),
                member.getRole() != null ? member.getRole().name() : null
        );
    }
}
