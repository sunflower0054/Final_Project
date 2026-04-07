package com.office.monitoring.dailyActivity;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DailyActivityMapper {

    // 저장
    void insert(DailyActivity dailyActivity);

    // 업데이트
    void update(DailyActivity dailyActivity);

    // 날짜 중복 체크
    Optional<DailyActivity> findByResidentIdAndDate(
            @org.apache.ibatis.annotations.Param("residentId") Long residentId,
            @org.apache.ibatis.annotations.Param("date") LocalDate date);

    // 날짜 범위 조회 (알림용)
    List<DailyActivity> findByResidentIdAndDateBetween(
            @org.apache.ibatis.annotations.Param("residentId") Long residentId,
            @org.apache.ibatis.annotations.Param("start") LocalDate start,
            @org.apache.ibatis.annotations.Param("end") LocalDate end);
}