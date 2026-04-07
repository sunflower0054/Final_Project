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

    private final DailyActivityMapper dailyActivityMapper;

    @Transactional
    public void save(Long residentId, LocalDate date, Integer motionScore) {

        dailyActivityMapper.findByResidentIdAndDate(residentId, date)
                .ifPresentOrElse(
                        existing -> {
                            //같은 날짜 데이터가 이미 있으면 중복 저장 대신 최신값으로 업데이트
                            log.warn("[DAILY 중복] residentId={} date={} → 업데이트", residentId, date);
                            existing.setMotionScore(motionScore);
                            dailyActivityMapper.update(existing);
                        },
                        () -> {
                            DailyActivity activity = DailyActivity.builder()
                                    .residentId(residentId)
                                    .date(date)
                                    .motionScore(motionScore)
                                    .build();
                            dailyActivityMapper.insert(activity);
                            log.info("[DAILY 저장] residentId={} date={} score={}", residentId, date, motionScore);
                        }
                );
    }
}