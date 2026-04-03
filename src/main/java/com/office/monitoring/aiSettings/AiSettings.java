package com.office.monitoring.aiSettings;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private Double fallSensitivity;

    @Column(name = "no_motion_threshold", nullable = false)
    private Integer noMotionThreshold;

    @Column(name = "velocity_threshold", nullable = false)
    private Double velocityThreshold;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
