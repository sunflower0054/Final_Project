package com.office.monitoring.event;


import com.office.monitoring.eventImage.EventImage;
import com.office.monitoring.eventImage.EventImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-dir-separator}")
    private String sep;

    @Transactional
    public Event receiveEvent(EventReceiveRequest req, MultipartFile frameImage) throws IOException {

        Long   residentId = Long.parseLong(req.getResidentId());
        Double confidence = Double.parseDouble(req.getConfidence());
        LocalDateTime timestamp = LocalDateTime.parse(
                req.getTimestamp(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        );

        String   metadata    = req.getMetadata();
        Integer  personCount = extractInt(metadata, "person_count");
        Double   maxVelocity = extractDouble(metadata, "max_velocity");
        LocalDateTime lastMotionTs = extractDateTime(metadata, "last_motion_timestamp");

        Event event = Event.builder()
                .residentId(residentId)
                .eventType(req.getEventType())
                .timestamp(timestamp)
                .confidence(confidence)
                .status("PENDING")
                .personCount(personCount)
                .maxVelocity(maxVelocity)
                .lastMotionTimestamp(lastMotionTs)
                .build();

        eventRepository.save(event);
        log.info("[EVENT 저장] id={} type={} resident={}", event.getId(), event.getEventType(), residentId);

        if (frameImage != null && !frameImage.isEmpty()) {

            // c:/upload  +  \\  +  events  +  \\  +  1  +  \\  +  2024-01-01  +  \\
            String datePath = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dirPath  = uploadDir + sep + "events" + sep + residentId + sep + datePath + sep;
            String fileName = event.getId() + "_" + System.currentTimeMillis() + ".jpg";
            String fullPath = dirPath + fileName;

            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            frameImage.transferTo(new File(fullPath));
            log.info("[IMAGE 저장] path={}", fullPath);

            EventImage image = EventImage.builder()
                    .event(event)
                    .imagePath(fullPath)
                    .build();

            eventImageRepository.save(image);
        }

        return event;
    }

    // ── metadata 파싱 헬퍼 ──────────────────────────────────────
    // 파이썬 str(dict) 형식: "{'person_count': 2, 'max_velocity': 0.05}"
    // 정규식 없이 단순 split으로 처리

    private Integer extractInt(String metadata, String key) {
        try {
            if (metadata == null || !metadata.contains(key)) return null;
            // ': 2, ...' 에서 콜론 뒤 첫 번째 숫자만 추출
            String after  = metadata.split("'" + key + "':\\s*")[1];  // '2, ...'
            String digits = after.split("[^0-9]")[0];                  // '2' 만
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return null;
        }
    }

    private Double extractDouble(String metadata, String key) {
        try {
            if (metadata == null || !metadata.contains(key)) return null;
            String after = metadata.split("'" + key + "':\\s*")[1];  // '0.11, ...'
            String value = after.split(",")[0]                        // '0.11'
                    .replaceAll("[^0-9.]", "").trim();
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime extractDateTime(String metadata, String key) {
        try {
            if (metadata == null || !metadata.contains(key)) return null;
            int start = metadata.indexOf(key) + key.length() + 4; // ': ' 포함
            String value = metadata.substring(start, start + 19); // "yyyy-MM-ddTHH:mm:ss"
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
}