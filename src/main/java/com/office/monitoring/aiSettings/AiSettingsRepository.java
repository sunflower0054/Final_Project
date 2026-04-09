package com.office.monitoring.aiSettings;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AiSettingsRepository extends JpaRepository<AiSettings, Long> {

    Optional<AiSettings> findByResidentId(Long residentId);

    void deleteByResidentId(Long residentId);

    boolean existsByResidentId(Long residentId);
}
