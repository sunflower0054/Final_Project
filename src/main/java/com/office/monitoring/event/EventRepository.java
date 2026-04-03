package com.office.monitoring.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 스케줄러용: PENDING 상태이면서 timeout 지난 이벤트 전체 조회
    List<Event> findAllByStatusAndCreatedAtBefore(String status, LocalDateTime threshold);
}