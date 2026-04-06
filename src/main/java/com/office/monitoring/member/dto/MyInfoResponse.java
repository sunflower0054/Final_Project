package com.office.monitoring.member.dto;

import com.office.monitoring.member.Member;

/** MyInfoResponse 타입을 정의한다. */
public record MyInfoResponse(
        boolean success,
        String username,
        String name,
        String phone,
        String purpose,
        String role
) {

    /** from 동작을 수행한다. */
    public static MyInfoResponse from(Member member) {
        return new MyInfoResponse(
                true,
                member.getUsername(),
                member.getName(),
                member.getPhone(),
                member.getPurpose(),
                member.getRole() != null ? member.getRole().name() : null
        );
    }
}
