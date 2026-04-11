package com.office.monitoring.notification;

import com.office.monitoring.dailyActivity.DailyActivity;
import com.office.monitoring.dailyActivity.DailyActivityRepository;
import com.office.monitoring.event.Event;
import com.office.monitoring.event.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EventRepository eventRepository;
    private final DailyActivityRepository dailyActivityRepository;

    private static final int DROP_THRESHOLD = 30;  // 30% 이상 감소 시 알림

    public NotificationDto checkFallActivityAlert(Long residentId) {

        // ── 1. 가장 최근 낙상 이벤트 조회 ───────────────────────────────────
        List<Event> fallEvents = eventRepository
                .findAllByResidentIdAndEventTypeAndStatusNot(residentId, "FALL_DETECTED", "PENDING");

        if (fallEvents.isEmpty()) {
            log.info("[알림] residentId={} 낙상 이벤트 없음", residentId);
            return NotificationDto.builder()
                    .triggered(false)
                    .message("최근 낙상 이벤트가 없습니다.")
                    .build();
        }

        // 가장 최근 낙상
        Event latestFall = fallEvents.stream()
                .max(Comparator.comparing(Event::getTimestamp))
                .orElse(null);

        if (latestFall == null) {
            return NotificationDto.builder()
                    .triggered(false)
                    .message("최근 낙상 이벤트가 없습니다.")
                    .build();
        }

        LocalDate fallDate = latestFall.getTimestamp().toLocalDate();
        log.info("[알림] 최근 낙상 날짜={}", fallDate);

        // ── 2. 낙상 이전 7일 평균 ────────────────────────────────────────────
        LocalDate beforeStart = fallDate.minusDays(7);
        LocalDate beforeEnd   = fallDate.minusDays(1);

        List<DailyActivity> beforeList = dailyActivityRepository
                .findByResidentIdAndDateBetween(residentId, beforeStart, beforeEnd);

        if (beforeList.isEmpty()) {
            log.warn("[알림] 낙상 이전 활동량 데이터 없음");
            return NotificationDto.builder()
                    .triggered(false)
                    .message("낙상 이전 활동량 데이터가 부족합니다.")
                    .build();
        }

        double avgBefore = beforeList.stream()
                .mapToInt(DailyActivity::getMotionScore)
                .average()
                .orElse(0);

        // ── 3. 낙상 이후 3일 평균 ────────────────────────────────────────────
        LocalDate afterEnd = fallDate.plusDays(3);

        List<DailyActivity> afterList = dailyActivityRepository
                .findByResidentIdAndDateBetween(residentId, fallDate, afterEnd);

        // 3일치 미만이면 수집 중 메시지
        if (afterList.size() < 3) {
            log.info("[알림] 낙상 이후 데이터 {}건 — 수집 중", afterList.size());
            return NotificationDto.builder()
                    .triggered(false)
                    .message("낙상 이후 데이터 수집 중입니다.")
                    .build();
        }

        double avgAfter = afterList.stream()
                .mapToInt(DailyActivity::getMotionScore)
                .average()
                .orElse(0);

        // ── 4. 감소율 계산 ───────────────────────────────────────────────────
        int dropPercent = (avgBefore > 0)
                ? (int) ((avgBefore - avgAfter) / avgBefore * 100)
                : 0;

        log.info("[알림] 이전평균={} 이후평균={} 감소율={}%", (int)avgBefore, (int)avgAfter, dropPercent);

        // ── 5. 알림 여부 판단 ────────────────────────────────────────────────
        boolean triggered = dropPercent >= DROP_THRESHOLD;

        String fallDateStr = fallDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fallTimeStr = latestFall.getTimestamp()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        String message = triggered
                ? String.format("낙상 발생(%s) 후 3일간 활동량이 %d%% 감소했습니다. 병원 방문을 권장합니다.",
                fallDateStr, dropPercent)
                : String.format("낙상 발생(%s) 후 활동량이 정상 범위입니다. (%d%% 감소)",
                fallDateStr, dropPercent);

        return NotificationDto.builder()
                .triggered(triggered)
                .fallDate(fallDateStr)
                .fallTime(fallTimeStr)
                .avgBefore((int) avgBefore)
                .avgAfter((int) avgAfter)
                .dropPercent(dropPercent)
                .message(message)
                .build();
    }
}