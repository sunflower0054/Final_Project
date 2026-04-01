package com.office.monitoring.dailyActivity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyActivityRepository extends JpaRepository<DailyActivity, Long> {

    // 같은 날짜 중복 방지용 (DB UNIQUE KEY와 이중 보호)
    Optional<DailyActivity> findByResidentIdAndDate(Long residentId, LocalDate date);
}