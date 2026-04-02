package com.office.monitoring.aiSettings;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AiSettingsRepository extends JpaRepository<AiSettings, Long> {

    boolean existsByResidentId(Long residentId);

    // resident_id로 설정값 조회
    AiSettings findByResidentId(Long residentId);
}
