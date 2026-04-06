package com.office.monitoring.member.dto;

/** WithdrawResponse 타입을 정의한다. */
public record WithdrawResponse(
        boolean success,
        String message
) {}