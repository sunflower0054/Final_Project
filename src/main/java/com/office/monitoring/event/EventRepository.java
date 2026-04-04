package com.office.monitoring.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 스케줄러용: PENDING 상태이면서 timeout 지난 이벤트 전체 조회
    List<Event> findAllByStatusAndCreatedAtBefore(String status, LocalDateTime threshold);

    Optional<Event> findTopByStatusOrderByCreatedAtDesc(String status);



    // 날짜 범위 + 거주자 + 상태 제외 조회 (N+1 방지 fetch join)
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.images " +
            "WHERE e.residentId = :residentId " +
            "AND e.createdAt BETWEEN :start AND :end " +
            "AND e.status != 'PENDING' " +
            "ORDER BY e.createdAt DESC")
    List<Event> findTimelineEvents(@Param("residentId") Long residentId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    // 7일 위험지수용: 날짜 범위 전체 조회
    @Query("SELECT e FROM Event e " +
            "WHERE e.residentId = :residentId " +
            "AND e.createdAt BETWEEN :start AND :end " +
            "AND e.status != 'PENDING'")
    List<Event> findByResidentIdAndCreatedAtBetween(@Param("residentId") Long residentId,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    // 이벤트 타입별 전체 조회
    List<Event> findAllByResidentIdAndEventTypeAndStatusNot(
            Long residentId, String eventType, String status);


    // 이벤트 타입별 전체 조회 (정렬 포함)
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.images " +
            "WHERE e.residentId = :residentId " +
            "AND e.eventType = :eventType " +
            "AND e.status != 'PENDING' " +
            "ORDER BY e.createdAt DESC")
    List<Event> findAllByTypeDesc(@Param("residentId") Long residentId,
                                  @Param("eventType") String eventType);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.images " +
            "WHERE e.residentId = :residentId " +
            "AND e.eventType = :eventType " +
            "AND e.status != 'PENDING' " +
            "ORDER BY e.createdAt ASC")
    List<Event> findAllByTypeAsc(@Param("residentId") Long residentId,
                                 @Param("eventType") String eventType);

}