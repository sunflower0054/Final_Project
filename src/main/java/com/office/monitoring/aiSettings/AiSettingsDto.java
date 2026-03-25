package com.office.monitoring.aiSettings;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AiSettingsDto {

    private Double fallSensitivity;      // 낙상 감지 민감도
    private Integer noMotionThreshold;   // 무응답 감지 시간 (초)
    private Double velocityThreshold;    // 폭행 의심 임계값
}