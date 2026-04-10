package com.office.monitoring.eventTimeline;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/eventsTimeline")
@RequiredArgsConstructor
public class EventTimelineController {

    private final EventTimelineService timelineService;

    // 하루치 타임라인
    @GetMapping()
    public ResponseEntity<EventTimelineDto> getTimeline(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long residentId) {

        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(timelineService.getTimeline(date, residentId));
    }

    // 이벤트 타입별 전체 조회
    @GetMapping("/all")
    public ResponseEntity<List<EventTimelineDto.EventItem>> getAllByType(
            @RequestParam String type,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(required = false) Long residentId) {
        return ResponseEntity.ok(timelineService.getAllByType(type, sort, residentId));
    }
}
