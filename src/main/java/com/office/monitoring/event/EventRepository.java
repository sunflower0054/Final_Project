package com.office.monitoring.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** 이벤트 조회와 통계용 projection 조회를 담당하는 리포지토리. */
public interface EventRepository extends JpaRepository<Event, Long> {

    /** 관리자 통계에서 사용하는 이벤트 projection이다. */
    interface AdminStatsView {
        /** 이벤트 유형 값을 반환한다. */
        String getEventType();

        /** 이벤트 상태 값을 반환한다. */
        String getStatus();

        /** 이벤트 발생 시각을 반환한다. */
        LocalDateTime getTimestamp();
    }

    /** timeout을 지난 PENDING 이벤트를 스케줄러 처리용으로 조회한다. */
    List<Event> findAllByStatusAndCreatedAtBefore(String status, LocalDateTime threshold);

    /** 가장 최근 생성된 특정 상태 이벤트를 1건 조회한다. */
    Optional<Event> findTopByStatusOrderByCreatedAtDesc(String status);

    /** 특정 거주자에 연결된 이벤트 존재 여부를 확인한다. */
    boolean existsByResidentId(Long residentId);

    /** 이미지까지 함께 읽어 타임라인 화면에 필요한 이벤트를 조회한다. */
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.images " +
            "WHERE e.residentId = :residentId " +
            "AND e.createdAt BETWEEN :start AND :end " +
            "AND e.status != 'PENDING' " +
            "ORDER BY e.createdAt DESC")
    List<Event> findTimelineEvents(@Param("residentId") Long residentId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    /** 위험지수 계산용으로 기간 내 이벤트를 조회한다. */
    @Query("SELECT e FROM Event e " +
            "WHERE e.residentId = :residentId " +
            "AND e.createdAt BETWEEN :start AND :end " +
            "AND e.status != 'PENDING'")
    List<Event> findByResidentIdAndCreatedAtBetween(@Param("residentId") Long residentId,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    /** 특정 거주자의 이벤트를 유형 기준으로 조회하되 특정 상태는 제외한다. */
    List<Event> findAllByResidentIdAndEventTypeAndStatusNot(
            Long residentId, String eventType, String status);

    /** 특정 유형 이벤트를 최신순으로 조회한다. */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.images " +
            "WHERE e.residentId = :residentId " +
            "AND e.eventType = :eventType " +
            "AND e.status != 'PENDING' " +
            "ORDER BY e.createdAt DESC")
    List<Event> findAllByTypeDesc(@Param("residentId") Long residentId,
                                  @Param("eventType") String eventType);

    /** 특정 유형 이벤트를 오래된 순으로 조회한다. */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.images " +
            "WHERE e.residentId = :residentId " +
            "AND e.eventType = :eventType " +
            "AND e.status != 'PENDING' " +
            "ORDER BY e.createdAt ASC")
    List<Event> findAllByTypeAsc(@Param("residentId") Long residentId,
                                 @Param("eventType") String eventType);

    /** 관리자 통계용 전체 이벤트 projection을 조회한다. */
    @Query("select e.eventType as eventType, e.status as status, e.timestamp as timestamp from Event e")
    List<AdminStatsView> findAllForAdminStats();

    /** 관리자 통계용 기간 필터 이벤트 projection을 조회한다. */
    @Query("select e.eventType as eventType, e.status as status, e.timestamp as timestamp " +
            "from Event e " +
            "where e.timestamp >= :start and e.timestamp < :end")
    List<AdminStatsView> findAllForAdminStatsBetween(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

}
