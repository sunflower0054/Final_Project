package com.office.monitoring.member.dto;

/** CheckUsernameResponse 타입을 정의한다. */
public record CheckUsernameResponse(
        boolean success,
        boolean available,
        String message
) {}
