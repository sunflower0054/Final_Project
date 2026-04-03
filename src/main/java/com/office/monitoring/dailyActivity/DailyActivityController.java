package com.office.monitoring.dailyActivity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/daily-activity")
@RequiredArgsConstructor
public class DailyActivityController {

    private final DailyActivityService dailyActivityService;

    @PostMapping
    public ResponseEntity<String> receive(
            @RequestParam("resident_id")  String residentId,
            @RequestParam("date")         String date,
            @RequestParam("motion_score") String motionScore
    ) {
        try {
            dailyActivityService.save(
                    Long.parseLong(residentId),
                    LocalDate.parse(date),
                    Integer.parseInt(motionScore)
            );
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("[DAILY 수신 오류]", e);
            return ResponseEntity.internalServerError().body("저장 실패");
        }
    }
}
