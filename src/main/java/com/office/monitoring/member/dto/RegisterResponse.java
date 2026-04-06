package com.office.monitoring.member.dto;

/** RegisterResponse 타입을 정의한다. */
public record RegisterResponse(
        boolean success,
        String message
) {}
