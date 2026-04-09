package com.office.monitoring.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/fall-activity-alert")
    public ResponseEntity<NotificationDto> getFallActivityAlert(
            @RequestParam(defaultValue = "22") Long residentId) {

        return ResponseEntity.ok(
                notificationService.checkFallActivityAlert(residentId));
    }
}