package com.office.monitoring.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
/** UpdateMyInfoRequest 타입을 정의한다. */
public record UpdateMyInfoRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        @NotBlank(message = "사용 목적은 필수입니다.")
        String purpose
) {}
