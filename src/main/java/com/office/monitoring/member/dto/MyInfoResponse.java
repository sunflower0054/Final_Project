package com.office.monitoring.member.dto;

import com.office.monitoring.member.Member;

public record MyInfoResponse(
        boolean success,
        MemberInfo member
) {
    public record MemberInfo(
            String username,
            String name,
            String phone,
            String purpose,
            String role,
            Integer birthYear,
            Long residentId
    ) {}

    public static MyInfoResponse from(Member member) {
        return new MyInfoResponse(
                true,
                new MemberInfo(
                        member.getUsername(),
                        member.getName(),
                        member.getPhone(),
                        member.getPurpose(),
                        member.getRole() != null ? member.getRole().name() : null,
                        member.getBirthYear(),
                        member.getResidentId()
                )
        );
    }
}
