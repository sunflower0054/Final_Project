package com.office.monitoring.aiSettings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class AiSettingController {

    private final AiSettingsService aiSettingsService;
    private static final Long TEMP_RESIDENT_ID = 24L;

    // 설정값 조회 (슬라이더 초기값 로딩)
    @GetMapping
    public ResponseEntity<AiSettingsDto> get() {
        return ResponseEntity.ok(aiSettingsService.get(TEMP_RESIDENT_ID));
    }

    // 설정값 변경 (슬라이더 변경 시 호출)
    @PutMapping
    public ResponseEntity<AiSettingsDto> update(@RequestBody AiSettingsDto dto) {
        return ResponseEntity.ok(aiSettingsService.save(TEMP_RESIDENT_ID, dto));
    }

    // FastAPI 즉시 전달
    @PostMapping("/apply")
    public ResponseEntity<Void> apply(@RequestBody AiSettingsDto dto) {
        aiSettingsService.applyToFastApi(dto);
        return ResponseEntity.ok().build();
    }
}
