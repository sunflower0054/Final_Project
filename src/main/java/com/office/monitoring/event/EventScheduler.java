package com.office.monitoring.event;

import com.office.monitoring.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;
    private final SmsService smsService;

    // ※ application.properties에서 값을 못 읽어오면 기본값으로 폴백
    @Value("${scheduler.timeout-ms:600000}")
    private long timeoutMs;

    // ※ 발표 시연 시 fixedDelay = 5000 (5초) 으로 변경
    @Scheduled(fixedDelay = 50000)
    public void checkPendingEvents() {
        LocalDateTime threshold = LocalDateTime.now()
                .minusNanos(timeoutMs * 1_000_000L);

        List<Event> pendingEvents =
                eventRepository.findAllByStatusAndCreatedAtBefore("PENDING", threshold);

        if (pendingEvents.isEmpty()) return;

        log.info("[스케줄러] PENDING 만료 이벤트 {}건 처리 시작", pendingEvents.size());

        for (Event event : pendingEvents) {
            processExpiredEvent(event);
        }
    }

    private void processExpiredEvent(Event event) {
        log.info("[스케줄러] eventId={} 자동 처리 시작", event.getId());

        // 1) status → AUTO_REPORTED
        event.setStatus("AUTO_REPORTED");
        eventRepository.save(event);

        // 2) 119 자동 문자 전송 시도
        boolean sent = smsService.sendAutoReport(event);

        if (sent) {
            // 성공 → 가족에게 2차 알림
            log.info("[스케줄러] eventId={} 119 자동신고 성공", event.getId());
            smsService.sendAutoReportAlert(event);

        } else {
            // 실패 → 지수백오프 x5 재시도
            log.warn("[스케줄러] eventId={} 119 자동신고 실패 → 재시도 시작", event.getId());
            boolean retrySent = smsService.sendAutoReportWithRetry(event, 5);

            if (retrySent) {
                log.info("[스케줄러] eventId={} 재시도 성공", event.getId());
                smsService.sendAutoReportAlert(event);
            } else {
                // 최종 실패 → SOS
                log.error("[스케줄러] eventId={} 재시도 전부 실패 → SOS", event.getId());
                event.setStatus("SOS_REPORT");
                eventRepository.save(event);
                smsService.sendSosAlert(event);
            }
        }
    }
}