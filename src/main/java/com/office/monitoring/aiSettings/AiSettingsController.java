package com.office.monitoring.aiSettings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class AiSettingsController {

    private final AiSettingsService aiSettingsService;

    // 현재 설정값 조회
    @GetMapping
    public ResponseEntity<?> getSettings() {
        try {
            AiSettings settings = aiSettingsService.getSettings();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "fallSensitivity",    settings.getFallSensitivity(),
                    "noMotionThreshold",  settings.getNoMotionThreshold(),
                    "velocityThreshold",  settings.getVelocityThreshold()
            ));
        } catch (Exception e) {
            log.error("❌ 설정값 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // 슬라이더 값 변경 시 호출
    @PutMapping
    public ResponseEntity<?> updateSettings(@RequestBody AiSettingsDto dto) {
        try {
            log.info("📩 설정값 변경 요청 | fall={} | motion={} | velocity={}",
                    dto.getFallSensitivity(),
                    dto.getNoMotionThreshold(),
                    dto.getVelocityThreshold());
            AiSettings saved = aiSettingsService.updateSettings(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "fallSensitivity",    saved.getFallSensitivity(),
                    "noMotionThreshold",  saved.getNoMotionThreshold(),
                    "velocityThreshold",  saved.getVelocityThreshold()
            ));
        } catch (Exception e) {
            log.error("❌ 설정값 변경 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}