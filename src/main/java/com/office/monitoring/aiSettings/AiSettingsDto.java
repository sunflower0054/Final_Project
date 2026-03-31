package com.office.monitoring.aiSettings;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AiSettingsDto {

    @NotNull
    @DecimalMin("0.0")
    private Double fallSensitivity;      // 낙상 감지 민감도

    @NotNull
    @Min(1)
    private Integer noMotionThreshold;   // 무응답 감지 시간 (초)

    @NotNull
    @DecimalMin("0.0")
    private Double velocityThreshold;    // 폭행 의심 임계값
}