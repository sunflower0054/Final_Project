package com.office.monitoring.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
/** 회원 정보 조회, 가입, 수정, 탈퇴와 관련된 도메인 동작을 담당하는 구성 요소. */
public record RegisterRequest(
        @NotBlank(message = "아이디는 필수입니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        @NotNull(message = "태어난 연도는 필수입니다.")
        @Min(value = 1900, message = "태어난 연도가 올바르지 않습니다.")
        @Max(value = 2100, message = "태어난 연도가 올바르지 않습니다.")
        Integer birthYear,

        @NotBlank(message = "사용 목적은 필수입니다.")
        String purpose
) {}
