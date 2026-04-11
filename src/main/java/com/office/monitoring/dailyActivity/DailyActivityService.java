package com.office.monitoring.dailyActivity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyActivityService {

    private final DailyActivityRepository dailyActivityRepository;

    @Transactional
    public void save(Long residentId, LocalDate date, Integer motionScore) {

        // DB UNIQUE KEY + 코드 레벨 이중 중복 방지
        dailyActivityRepository.findByResidentIdAndDate(residentId, date)
                .ifPresentOrElse(
                        existing -> {
                            log.warn("[DAILY 중복] residentId={} date={} → 업데이트", residentId, date);
                            existing.setMotionScore(motionScore);
                        },
                        () -> {
                            DailyActivity activity = DailyActivity.builder()
                                    .residentId(residentId)
                                    .date(date)
                                    .motionScore(motionScore)
                                    .build();
                            dailyActivityRepository.save(activity);
                            log.info("[DAILY 저장] residentId={} date={} score={}", residentId, date, motionScore);
                        }
                );
    }
}
