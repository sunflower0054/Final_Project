package com.office.monitoring.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(
        String username,
        String password,
        String name,
        String phone,
        String purpose
) {}
