package com.office.monitoring.resident;

import org.springframework.data.jpa.repository.JpaRepository;

/** 거주자 데이터 조회·등록·수정·삭제 흐름을 담당하는 구성 요소. */
public interface ResidentRepository extends JpaRepository<Resident, Long> {
}
