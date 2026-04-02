package com.office.monitoring.aiSettings;

import com.office.monitoring.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSettingsService {

    private final AiSettingsRepository aiSettingsRepository;
    private final CurrentUserService currentUserService;

    private static final String PYTHON_URL = "http://localhost:5005";

    public AiSettings getSettings() {
        Long residentId = currentUserService.getResidentId();

        AiSettings settings = aiSettingsRepository.findByResidentId(residentId);
        if (settings == null) {
            throw new IllegalStateException("해당 거주자의 AI 설정이 존재하지 않습니다.");
        }

        return settings;
    }

    public AiSettings updateSettings(AiSettingsDto dto) {
        Long residentId = currentUserService.getResidentId();

        AiSettings settings = aiSettingsRepository.findByResidentId(residentId);

        if (settings == null) {
            settings = AiSettings.builder()
                    .residentId(residentId)
                    .fallSensitivity(0.1D)
                    .noMotionThreshold(1800)
                    .velocityThreshold(0.15D)
                    .build();
        }

        settings.setFallSensitivity(dto.getFallSensitivity());
        settings.setNoMotionThreshold(dto.getNoMotionThreshold());
        settings.setVelocityThreshold(dto.getVelocityThreshold());

        AiSettings saved = aiSettingsRepository.save(settings);

        log.info("설정값 DB 저장 완료 | residentId={} | fall={} | motion={} | velocity={}",
                residentId,
                saved.getFallSensitivity(),
                saved.getNoMotionThreshold(),
                saved.getVelocityThreshold());

        try {
            WebClient client = WebClient.create(PYTHON_URL);
            client.post()
                    .uri("/api/settings")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(response ->
                            log.info("파이썬 설정값 전달 완료: {}", response)
                    );
        } catch (Exception e) {
            log.error("파이썬 설정값 전달 실패", e);
        }

        return saved;
    }
}
