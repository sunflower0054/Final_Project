package com.office.monitoring.aiSettings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSettingsService {

    private final AiSettingsRepository aiSettingsRepository;

    private static final Long RESIDENT_ID = 1L;
    private static final String PYTHON_URL = "http://localhost:5005";

    // 설정값 조회
    public AiSettings getSettings() {
        return aiSettingsRepository.findByResidentId(RESIDENT_ID);
    }

    // 설정값 저장 + 파이썬으로 즉시 전달
    public AiSettings updateSettings(AiSettingsDto dto) {

        // 1. DB 저장
        AiSettings settings = aiSettingsRepository.findByResidentId(RESIDENT_ID);
        settings.setFallSensitivity(dto.getFallSensitivity());
        settings.setNoMotionThreshold(dto.getNoMotionThreshold());
        settings.setVelocityThreshold(dto.getVelocityThreshold());
        AiSettings saved = aiSettingsRepository.save(settings);
        log.info("✅ 설정값 DB 저장 완료 | fall={} | motion={} | velocity={}",
                saved.getFallSensitivity(),
                saved.getNoMotionThreshold(),
                saved.getVelocityThreshold());

        // 2. 파이썬으로 즉시 POST 전달
        try {
            WebClient client = WebClient.create(PYTHON_URL);
            client.post()
                    .uri("/api/settings")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(response ->
                            log.info("✅ 파이썬 설정값 전달 완료: {}", response)
                    );
        } catch (Exception e) {
            log.error("❌ 파이썬 설정값 전달 실패", e);
        }

        return saved;
    }
}