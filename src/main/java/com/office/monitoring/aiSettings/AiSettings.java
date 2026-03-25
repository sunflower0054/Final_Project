package com.office.monitoring.aiSettings;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_settings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "fall_sensitivity", nullable = false)
    private Double fallSensitivity;      // 낙상 감지 민감도

    @Column(name = "no_motion_threshold", nullable = false)
    private Integer noMotionThreshold;   // 무응답 감지 시간 (초)

    @Column(name = "velocity_threshold", nullable = false)
    private Double velocityThreshold;    // 폭행 의심 임계값
}