package com.office.monitoring.resident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

/** 거주자 데이터 조회·등록·수정·삭제 흐름을 담당하는 구성 요소. */
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    interface AdminStatsView {
        LocalDate getBirthDate();
    }

    @Query("select r.birthDate as birthDate from Resident r")
    List<AdminStatsView> findAllForAdminStats();
}
