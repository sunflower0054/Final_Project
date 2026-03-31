package com.office.monitoring.member.dto;

public record WithdrawResponse(
        boolean success,
        String message
) {}