package com.office.monitoring.event;

import com.office.monitoring.eventImage.EventImage;
import com.office.monitoring.eventImage.EventImageRepository;
import com.office.monitoring.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventApiController {

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final SmsService smsService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-dir-separator}")
    private String sep;

    @PostMapping("/receive")
    public ResponseEntity<Void> receiveEvent(
            @RequestParam("resident_id")                          String residentId,
            @RequestParam("event_type")                           String eventType,
            @RequestParam("timestamp")                            String timestamp,
            @RequestParam("confidence")                           String confidence,
            @RequestParam(value = "metadata",    required = false) String metadata,
            @RequestParam(value = "frame_image", required = false) MultipartFile frameImage) {

        log.info("[이벤트 수신] type={}, residentId={}, timestamp={}", eventType, residentId, timestamp);

        // ── 1. metadata 파싱 ─────────────────────────────────────────────────
        Integer personCount = null;
        Double  maxVelocity = null;
        LocalDateTime lastMotionTimestamp = null;

        if (metadata != null && !metadata.isBlank()) {
            personCount       = parseIntFromMeta(metadata, "person_count");
            maxVelocity       = parseDoubleFromMeta(metadata, "max_velocity");
            String lastMotion = parseStringFromMeta(metadata, "last_motion_timestamp");
            if (lastMotion != null) {
                try { lastMotionTimestamp = LocalDateTime.parse(lastMotion); }
                catch (Exception ignored) {}
            }
        }

        // ── 2. Event 저장 ────────────────────────────────────────────────────
        Event event = Event.builder()
                .residentId(Long.parseLong(residentId))
                .eventType(eventType)
                .timestamp(LocalDateTime.parse(timestamp))
                .confidence(parseDoubleSafe(confidence))
                .personCount(personCount)
                .maxVelocity(maxVelocity)
                .lastMotionTimestamp(lastMotionTimestamp)
                .status("PENDING")
                .build();

        eventRepository.save(event);
        log.info("[이벤트 수신] 저장 완료 eventId={}", event.getId());

        // ── 3. 이미지 저장 ───────────────────────────────────────────────────
        if (frameImage != null && !frameImage.isEmpty()) {
            try {
                String datePath = LocalDateTime.parse(timestamp)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String dirPath  = uploadDir + sep + "events" + sep
                        + residentId + sep + datePath + sep;
                String fileName = event.getId() + "_" + System.currentTimeMillis() + ".jpg";
                String fullPath = dirPath + fileName; // 실제 저장 경로
                String urlPath  = "/uploaded/events/" + residentId + "/" + datePath + "/" + fileName;  // URL 경로

                File dir = new File(dirPath);
                if (!dir.exists()) dir.mkdirs();
                frameImage.transferTo(new File(fullPath));

                eventImageRepository.save(EventImage.builder()
                        .event(event)
                        .imagePath(urlPath)
                        .build());
                log.info("[이미지 저장] eventId={} path={}", event.getId(), fullPath);

            } catch (Exception e) {
                log.error("[이미지 저장 실패] eventId={} 오류: {}", event.getId(), e.getMessage());
            }
        }

        // ── 4. 1차 가족 알림 ─────────────────────────────────────────────────
        smsService.sendFirstAlert(event);
        log.info("[이벤트 수신] 1차 문자 전송 완료 eventId={}", event.getId());

        return ResponseEntity.ok().build();
    }

    // ── 파싱 유틸 ────────────────────────────────────────────────────────────

    private Integer parseIntFromMeta(String meta, String key) {
        try {
            Matcher m = Pattern.compile(key + "['\"]?\\s*:\\s*(\\d+)").matcher(meta);
            return m.find() ? Integer.parseInt(m.group(1)) : null;
        } catch (Exception e) { return null; }
    }

    private Double parseDoubleFromMeta(String meta, String key) {
        try {
            Matcher m = Pattern.compile(key + "['\"]?\\s*:\\s*([\\d.]+)").matcher(meta);
            return m.find() ? Double.parseDouble(m.group(1)) : null;
        } catch (Exception e) { return null; }
    }

    private String parseStringFromMeta(String meta, String key) {
        try {
            Matcher m = Pattern.compile(key + "['\"]?\\s*:\\s*['\"]?([^,'\"{}]+)['\"]?").matcher(meta);
            return m.find() ? m.group(1).trim() : null;
        } catch (Exception e) { return null; }
    }

    private Double parseDoubleSafe(String val) {
        try { return Double.parseDouble(val); }
        catch (Exception e) { return null; }
    }

    @GetMapping("/latest-pending")
    public ResponseEntity<Event> getLatestPendingEvent(@RequestParam Long residentId) {
        return eventRepository.findTopByResidentIdAndStatusInOrderByCreatedAtDesc(
                        residentId, List.of("PENDING"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}