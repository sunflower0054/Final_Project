package com.office.monitoring.member.dto;

import com.office.monitoring.member.Member;

/** 회원 정보 조회, 가입, 수정, 탈퇴와 관련된 도메인 동작을 담당하는 구성 요소. */
public record MyInfoResponse(
        boolean success,
        String username,
        String name,
        String phone,
        Integer birthYear,
        String purpose,
        String role
) {

    /** 요청/엔티티 데이터를 다른 표현 객체로 변환해 반환한다. */
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
