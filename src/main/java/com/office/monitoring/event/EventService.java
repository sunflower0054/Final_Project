package com.office.monitoring.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private static final String IMAGE_DIR = System.getProperty("user.dir") + "/uploads/events/";

    public Event receiveEvent(EventReceiveDto dto) throws Exception {

        // 1. 이미지 저장
        String imagePath = saveImage(dto.getFrame_image(), dto.getEvent_type());

        // 2. metadata 파싱
        String metadata = dto.getMetadata();
        LocalDateTime lastMotionTimestamp = null;
        Integer personCount = null;
        Double maxVelocity = null;

        if (metadata != null && !metadata.equals("{}")) {
            // 파이썬이 작은따옴표로 보내니까 큰따옴표로 변환
            metadata = metadata.replace("'", "\"");

            // FA-002 무응답 — last_motion_timestamp 추출
            if (metadata.contains("last_motion_timestamp")) {
                String value = metadata
                        .split("\"last_motion_timestamp\"\\s*:\\s*\"")[1]  // ← 키:값 구분자 통째로 제거
                        .split("\"")[0]  // ← 닫는 따옴표 앞까지만 추출
                        .trim();
                lastMotionTimestamp = LocalDateTime.parse(
                        value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                log.info("last_motion_timestamp 파싱 완료: {}", lastMotionTimestamp);
            }

            // FA-003 폭행 — person_count 추출
            if (metadata.contains("person_count")) {
                String pcValue = metadata
                        .split("\"person_count\"\\s*:\\s*")[1]  // ← 키: 제거
                        .split("[,}]")[0]  // ← , 또는 } 앞까지만 추출
                        .trim();
                personCount = Integer.parseInt(pcValue);
                log.info("person_count 파싱 완료: {}", personCount);
            }

            // FA-003 폭행 — max_velocity 추출
            if (metadata.contains("max_velocity")) {
                String mvValue = metadata
                        .split("\"max_velocity\"\\s*:\\s*")[1]  // ← 키: 제거
                        .split("[,}]")[0]  // ← , 또는 } 앞까지만 추출
                        .trim();
                maxVelocity = Double.parseDouble(mvValue);
                log.info("max_velocity 파싱 완료: {}", maxVelocity);
            }
        }

        // 3. Event 엔티티 빌드
        Event event = Event.builder()
                .residentId(Long.parseLong(dto.getResident_id()))
                .eventType(dto.getEvent_type())
                .timestamp(LocalDateTime.parse(
                        dto.getTimestamp(),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .confidence(Double.parseDouble(dto.getConfidence()))
                .status("PENDING")
                .lastMotionTimestamp(lastMotionTimestamp)  // FA-002
                .personCount(personCount)                  // FA-003
                .maxVelocity(maxVelocity)                  // FA-003
                .build();

        // 4. DB 저장
        Event saved = eventRepository.save(event);

        log.info("✅ 이벤트 저장 완료 | id={} | type={} | imagePath={}",
                saved.getId(), saved.getEventType(), imagePath);

        return saved;
    }

    private String saveImage(MultipartFile file, String eventType) {
        if (file == null || file.isEmpty()) return null;
        try {
            File dir = new File(IMAGE_DIR);
            if (!dir.exists()) dir.mkdirs();

            String filename = eventType + "_"
                    + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".jpg";

            File dest = new File(IMAGE_DIR + filename);
            file.transferTo(dest);
            return IMAGE_DIR + filename;

        } catch (Exception e) {
            log.error("❌ 이미지 저장 실패", e);
            return null;
        }
    }
}
