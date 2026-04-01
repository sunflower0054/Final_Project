package com.office.monitoring.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventAPIController {

    private final EventService eventService;

    @PostMapping(value = "/receive", consumes = "multipart/form-data")
    public ResponseEntity<?> receiveEvent(@ModelAttribute EventReceiveDto dto) {
        try {
            log.info("📩 이벤트 수신 | type={} | resident={}", dto.getEvent_type(), dto.getResident_id());
            Event saved = eventService.receiveEvent(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "eventId", saved.getId(),
                    "status", saved.getStatus()
            ));
        } catch (Exception e) {
            log.error("❌ 이벤트 수신 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}