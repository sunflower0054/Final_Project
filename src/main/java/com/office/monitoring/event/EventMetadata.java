package com.office.monitoring.event;

import java.time.LocalDateTime;

public record EventMetadata(
        LocalDateTime lastMotionTimestamp,
        Integer personCount,
        Double maxVelocity
) {
    public static EventMetadata empty() {
        return new EventMetadata(null, null, null);
    }
}
