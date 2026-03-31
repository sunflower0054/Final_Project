package com.office.monitoring.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateMyInfoRequest(
        String name,
        String phone,
        String purpose
) {}
