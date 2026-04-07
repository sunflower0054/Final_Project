package com.office.monitoring.notification;

import com.office.monitoring.dailyActivity.DailyActivity;
import com.office.monitoring.dailyActivity.DailyActivityMapper;  // ← 변경
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
    private final DailyActivityMapper dailyActivityMapper;  // ← 변경

    private static final int DROP_THRESHOLD = 30;

    public NotificationDto checkFallActivityAlert(Long residentId) {

        // ── 1. 가장 최근 낙상 이벤트 조회 ───────────────────────────────────
        List<Event> fallEvents = eventRepository
                .findAllByResidentIdAndEventTypeAndStatusNot(residentId, "FALL_DETECTED", "PENDING");

        if (fallEvents.isEmpty()) {
            return NotificationDto.builder()
                    .triggered(false)
                    .message("최근 낙상 이벤트가 없습니다.")
                    .build();
        }

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

        // ── 2. 낙상 이전 7일 평균 ────────────────────────────────────────────
        List<DailyActivity> beforeList = dailyActivityMapper
                .findByResidentIdAndDateBetween(residentId,
                        fallDate.minusDays(7), fallDate.minusDays(1));  // ← 변경

        if (beforeList.isEmpty()) {
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
        List<DailyActivity> afterList = dailyActivityMapper
                .findByResidentIdAndDateBetween(residentId,
                        fallDate, fallDate.plusDays(3));  // ← 변경

        if (afterList.size() < 3) {
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