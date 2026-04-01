package com.office.monitoring.event;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EventReceiveRequest {
    private String residentId;      // "1"
    private String eventType;       // "FALL_DETECTED"
    private String timestamp;       // "2024-01-01T12:00:00"
    private String confidence;      // "0.85"
    private String metadata;        // "{'person_count': 2, 'max_velocity': 0.05}"
}