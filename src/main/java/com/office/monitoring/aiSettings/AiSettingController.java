package com.office.monitoring.aiSettings;

import com.office.monitoring.security.CurrentUserService;
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
    private final CurrentUserService currentUserService;   // ← 추가

    // 설정값 조회
    @GetMapping
    public ResponseEntity<AiSettingsDto> get() {
        Long residentId = currentUserService.getResidentId();
        return ResponseEntity.ok(aiSettingsService.get(residentId));
    }

    // 설정값 변경
    @PutMapping
    public ResponseEntity<AiSettingsDto> update(@RequestBody AiSettingsDto dto) {
        Long residentId = currentUserService.getResidentId();
        return ResponseEntity.ok(aiSettingsService.save(residentId, dto));
    }

    // FastAPI 즉시 전달
    @PostMapping("/apply")
    public ResponseEntity<Void> apply(@RequestBody AiSettingsDto dto) {
        Long residentId = currentUserService.getResidentId();   // 필요하면 service에 전달 가능
        aiSettingsService.applyToFastApi(dto);
        return ResponseEntity.ok().build();
    }
}