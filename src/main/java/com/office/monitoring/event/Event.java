package com.office.monitoring.event;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "status", nullable = false)
    private String status;

    // FA-002 전용
    @Column(name = "last_motion_timestamp")
    private LocalDateTime lastMotionTimestamp;

    // FA-003 전용
    @Column(name = "person_count")
    private Integer personCount;

    @Column(name = "max_velocity")
    private Double maxVelocity;
}