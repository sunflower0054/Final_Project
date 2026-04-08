package com.office.monitoring.resident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

/** 거주자 데이터 조회·등록·수정·삭제 흐름을 담당하는 구성 요소. */
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    /** 관리자 거주자 통계에서 사용하는 projection이다. */
    interface AdminStatsView {
        /** 생년월일 값을 반환한다. */
        LocalDate getBirthDate();
    }

    /** 관리자 통계에 필요한 최소 거주자 컬럼만 projection으로 조회한다. */
    @Query("select r.birthDate as birthDate from Resident r")
    List<AdminStatsView> findAllForAdminStats();
}
