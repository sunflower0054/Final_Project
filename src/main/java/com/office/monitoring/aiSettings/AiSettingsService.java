package com.office.monitoring.aiSettings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSettingsService {

    private final AiSettingsRepository aiSettingsRepository;

    @Value("${fastapi.url}")
    private String fastapiUrl;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── PUT /api/ai-settings — DB 저장만
    @Transactional
    public AiSettingsDto save(Long residentId, AiSettingsDto dto) {
        AiSettings settings = aiSettingsRepository.findByResidentId(residentId)
                .orElse(AiSettings.builder().residentId(residentId).build());

        settings.setFallSensitivity(dto.getFallSensitivity());
        settings.setNoMotionThreshold(dto.getNoMotionThreshold());
        settings.setVelocityThreshold(dto.getVelocityThreshold());
        settings.setUpdatedAt(LocalDateTime.now());
        aiSettingsRepository.save(settings);

        log.info("[AI설정 저장] residentId={}", residentId);
        dto.setUpdatedAt(settings.getUpdatedAt().format(FORMATTER));
        return dto;
    }

    // ── POST /api/ai-settings/apply — FastAPI 전달만
    public void applyToFastApi(AiSettingsDto dto) {
        try {
            WebClient.create(fastapiUrl)
                    .post()
                    .uri("/api/settings")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("[FastAPI 전달 완료]");
        } catch (Exception e) {
            log.error("[FastAPI 전달 실패] {}", e.getMessage());
        }
    }

    // ── GET /api/ai-settings — 조회
    public AiSettingsDto get(Long residentId) {
        AiSettings settings = aiSettingsRepository.findByResidentId(residentId)
                .orElseThrow(() -> new RuntimeException("AI 설정이 존재하지 않습니다."));

        AiSettingsDto dto = new AiSettingsDto();
        dto.setFallSensitivity(settings.getFallSensitivity());
        dto.setNoMotionThreshold(settings.getNoMotionThreshold());
        dto.setVelocityThreshold(settings.getVelocityThreshold());
        dto.setUpdatedAt(settings.getUpdatedAt() != null
                ? settings.getUpdatedAt().format(FORMATTER) : "-");
        return dto;
    }
}