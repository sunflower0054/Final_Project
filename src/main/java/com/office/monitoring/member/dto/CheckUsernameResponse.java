package com.office.monitoring.member.dto;

public record CheckUsernameResponse(
        boolean success,
        boolean available,
        String message
) {}
