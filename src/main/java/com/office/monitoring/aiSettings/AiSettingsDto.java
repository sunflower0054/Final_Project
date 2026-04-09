package com.office.monitoring.aiSettings;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AiSettingsDto {

    private Double fallSensitivity;
    private Integer noMotionThreshold;
    private Double velocityThreshold;
    private String  updatedAt;
}
