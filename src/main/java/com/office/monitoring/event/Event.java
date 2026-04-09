package com.office.monitoring.event;

import com.office.monitoring.eventImage.EventImage;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;               // FALL_DETECTED / NO_MOTION_DETECTED / VIOLENT_MOTION_DETECTED

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;        // 파이썬이 보낸 감지 시각

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";      // PENDING / CONFIRMED / CLOSED / AUTO_REPORTED

    @Column(name = "last_motion_timestamp")
    private LocalDateTime lastMotionTimestamp;  // NO_MOTION_DETECTED 전용

    @Column(name = "person_count")
    private Integer personCount;            // VIOLENT_MOTION_DETECTED 전용

    @Column(name = "max_velocity")
    private Double maxVelocity;             // VIOLENT_MOTION_DETECTED 전용

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventImage> images = new ArrayList<>();
}