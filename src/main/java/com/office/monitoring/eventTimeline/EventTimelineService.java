package com.office.monitoring.eventTimeline;

import com.office.monitoring.event.Event;
import com.office.monitoring.event.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventTimelineService {

    private final EventRepository eventRepository;

    private static final Long TEMP_RESIDENT_ID = 22L;

    // 하루치 타임라인 조회
    public EventTimelineDto getTimeline(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.atTime(23, 59, 59);

        // 1) 하루치 이벤트 (PENDING 제외, 이미지 fetch join)
        List<Event> events = eventRepository.findTimelineEvents(
                TEMP_RESIDENT_ID, start, end);

        // 2) 7일 위험지수 계산
        LocalDateTime weekStart = date.minusDays(6).atStartOfDay();
        List<Event> weekEvents  = eventRepository
                .findByResidentIdAndCreatedAtBetween(TEMP_RESIDENT_ID, weekStart, end);
        int riskScore = calcRiskScore(weekEvents);

        // 3) 하루 요약
        EventTimelineDto.DaySummary daySummary = EventTimelineDto.DaySummary.builder()
                .totalCount((int) events.size())
                .fallCount((int) events.stream()
                        .filter(e -> e.getEventType().equals("FALL_DETECTED")).count())
                .noMotionCount((int) events.stream()
                        .filter(e -> e.getEventType().equals("NO_MOTION_DETECTED")).count())
                .violentCount((int) events.stream()
                        .filter(e -> e.getEventType().equals("VIOLENT_MOTION_DETECTED")).count())
                .build();

        // 4) 이벤트 목록 변환
        List<EventTimelineDto.EventItem> items = events.stream()
                .map(this::toEventItem)
                .collect(Collectors.toList());

        // 5) 날짜 범위 문자열 (3/19 ~ 3/25)
        String dateRange = (date.minusDays(6).getMonthValue()) + "/"
                + (date.minusDays(6).getDayOfMonth()) + " ~ "
                + date.getMonthValue() + "/" + date.getDayOfMonth();

        return EventTimelineDto.builder()
                .date(date.toString())
                .riskSummary(EventTimelineDto.RiskSummary.builder()
                        .totalScore(riskScore)
                        .riskLevel(calcRiskLevel(riskScore))
                        .dateRange(dateRange)
                        .build())
                .daySummary(daySummary)
                .events(items)
                .build();
    }

    // 이벤트 타입별 전체 조회
    public List<EventTimelineDto.EventItem> getAllByType(String eventType, String sort) {
        List<Event> events = sort.equals("asc")
                ? eventRepository.findAllByTypeAsc(TEMP_RESIDENT_ID, eventType)
                : eventRepository.findAllByTypeDesc(TEMP_RESIDENT_ID, eventType);
        return events.stream().map(this::toEventItem).collect(Collectors.toList());
    }

    // ── 내부 유틸 ──────────────────────────────────────

    private EventTimelineDto.EventItem toEventItem(Event e) {
        String imagePath = e.getImages().isEmpty()
                ? null : e.getImages().get(0).getImagePath();

        return EventTimelineDto.EventItem.builder()
                .id(e.getId())
                .eventType(e.getEventType())
                .timestamp(e.getTimestamp()
                        .format(DateTimeFormatter.ofPattern("HH:mm")))
                .date(e.getTimestamp()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .status(toStatusLabel(e.getStatus()))
                .imagePath(imagePath)
                .lastMotionTimestamp(e.getLastMotionTimestamp())
                .personCount(e.getPersonCount())
                .maxVelocity(e.getMaxVelocity())
                .build();
    }

    private String toStatusLabel(String status) {
        return switch (status) {
            case "CONFIRMED"    -> "신고완료 (가족신고)";
            case "AUTO_REPORTED"-> "신고완료 (자동신고)";
            case "CLOSED"       -> "정상확인 (이상없음)";
            case "SOS_REPORT"   -> "신고완료 (자동신고)";
            default             -> status;
        };
    }

    private int calcRiskScore(List<Event> events) {
        int score = 0;
        for (Event e : events) {
            score += switch (e.getEventType()) {
                case "FALL_DETECTED"            -> 20;
                case "NO_MOTION_DETECTED"       -> 20;
                case "VIOLENT_MOTION_DETECTED"  -> 30;
                default -> 0;
            };
        }
        return Math.min(score, 200);
    }

    private String calcRiskLevel(int score) {
        if (score < 20)  return "안전";
        if (score < 70)  return "주의";
        return "위험";
    }
}
