package com.office.monitoring.member;

import org.springframework.data.jpa.repository.JpaRepository;

/** 회원 정보 조회, 가입, 수정, 탈퇴와 관련된 도메인 동작을 담당하는 구성 요소. */
public interface WithdrawnUserRepository extends JpaRepository<WithdrawnUser, Long> {
}
