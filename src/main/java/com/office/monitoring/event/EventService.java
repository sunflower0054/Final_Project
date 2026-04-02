package com.office.monitoring.event;

import com.office.monitoring.resident.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final ResidentRepository residentRepository;
    private final JsonMapper objectMapper;

    private static final Path IMAGE_DIR =
            Paths.get(System.getProperty("user.dir"), "uploads", "events");

    public Event receiveEvent(EventReceiveDto dto) {
        Long residentId = parseRequiredLong(dto.getResident_id(), "resident_id");
        residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 거주자입니다."));

        String eventType = parseRequiredText(dto.getEvent_type(), "event_type");
        LocalDateTime timestamp = parseRequiredDateTime(dto.getTimestamp(), "timestamp");
        Double confidence = parseOptionalDouble(dto.getConfidence(), "confidence");
        EventMetadata metadata = parseMetadata(dto.getMetadata());

        String imagePath = saveImage(dto.getFrame_image(), eventType);

        try {
            Event event = Event.builder()
                    .residentId(residentId)
                    .eventType(eventType)
                    .timestamp(timestamp)
                    .confidence(confidence)
                    .status("PENDING")
                    .lastMotionTimestamp(metadata.lastMotionTimestamp())
                    .personCount(metadata.personCount())
                    .maxVelocity(metadata.maxVelocity())
                    .build();

            Event saved = eventRepository.save(event);

            eventImageRepository.save(EventImage.builder()
                    .eventId(saved.getId())
                    .imagePath(imagePath)
                    .build());

            log.info("이벤트 저장 완료 | id={} | type={} | residentId={} | imagePath={}",
                    saved.getId(), saved.getEventType(), saved.getResidentId(), imagePath);

            return saved;
        } catch (RuntimeException e) {
            deleteQuietly(imagePath);
            throw e;
        }
    }

    private EventMetadata parseMetadata(String metadata) {
        if (metadata == null || metadata.isBlank() || "{}".equals(metadata.trim())) {
            return EventMetadata.empty();
        }

        JsonNode root = readMetadataJson(metadata);

        return new EventMetadata(
                parseOptionalDateTime(root, "last_motion_timestamp"),
                parseOptionalInteger(root, "person_count"),
                parseOptionalDouble(root, "max_velocity")
        );
    }

    private JsonNode readMetadataJson(String metadata) {
        try {
            return objectMapper.readTree(metadata);
        } catch (Exception firstException) {
            String normalized = metadata.replace('\'', '"');
            try {
                return objectMapper.readTree(normalized);
            } catch (Exception secondException) {
                throw new IllegalArgumentException("metadata는 올바른 JSON 형식이어야 합니다.");
            }
        }
    }

    private LocalDateTime parseOptionalDateTime(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }

        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " 값이 올바른 날짜시간 형식이 아닙니다.");
        }
    }

    private Integer parseOptionalInteger(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }

        try {
            if (node.isNumber()) {
                return node.intValue();
            }
            return Integer.parseInt(node.asText().trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " 값이 올바른 정수가 아닙니다.");
        }
    }

    private Double parseOptionalDouble(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }

        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            if (node.isNumber()) {
                return node.doubleValue();
            }
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " 값이 올바른 실수가 아닙니다.");
        }
    }

    private Long parseRequiredLong(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 는 필수입니다.");
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " 값이 올바른 숫자가 아닙니다.");
        }
    }

    private Double parseOptionalDouble(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " 값이 올바른 실수가 아닙니다.");
        }
    }

    private LocalDateTime parseRequiredDateTime(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 는 필수입니다.");
        }

        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " 값이 올바른 날짜시간 형식이 아닙니다.");
        }
    }

    private String parseRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 는 필수입니다.");
        }
        return value.trim();
    }

    private String saveImage(MultipartFile file, String eventType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("frame_image 는 필수입니다.");
        }

        try {
            Files.createDirectories(IMAGE_DIR);

            String extension = extractExtension(file.getOriginalFilename());
            String fileName = eventType + "_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + "_" + UUID.randomUUID().toString().substring(0, 8)
                    + extension;

            Path targetPath = IMAGE_DIR.resolve(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return "uploads/events/" + fileName;
        } catch (IOException e) {
            log.error("이미지 저장 실패", e);
            throw new IllegalStateException("이벤트 이미지 저장에 실패했습니다.");
        }
    }

    private void deleteQuietly(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return;
        }

        try {
            Path path = Paths.get(System.getProperty("user.dir")).resolve(imagePath).normalize();
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return ".jpg";
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return ".jpg";
        }

        String extension = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (extension.length() > 10) {
            return ".jpg";
        }
        return extension;
    }
}
