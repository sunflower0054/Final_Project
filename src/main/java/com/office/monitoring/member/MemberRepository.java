package com.office.monitoring.member;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** 회원 정보 조회, 가입, 수정, 탈퇴와 관련된 도메인 동작을 담당하는 구성 요소. */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /** 관리자 회원 통계에서 사용하는 projection이다. */
    interface AdminStatsView {
        /** 출생연도 값을 반환한다. */
        Integer getBirthYear();

        /** 가입 목적 값을 반환한다. */
        String getPurpose();

        LocalDateTime getCreatedAt();
    }

    /** 사용자 아이디로 회원 1건을 조회한다. */
    Optional<Member> findByUsername(String username);

    /** 특정 거주자와 연결된 회원 목록을 조회한다. */
    List<Member> findByResidentId(Long residentId);

    /** 특정 거주자와 연결된 회원 목록을 전체 조회한다. */
    List<Member> findAllByResidentId(Long residentId);

    /** 관리자 통계에 필요한 최소 회원 컬럼만 projection으로 조회한다. */
    @Query("select m.birthYear as birthYear, m.purpose as purpose, m.createdAt as createdAt from Member m")
    List<AdminStatsView> findAllForAdminStats();

    @Query("select m.birthYear as birthYear, m.purpose as purpose, m.createdAt as createdAt " +
            "from Member m " +
            "where m.createdAt >= :start and m.createdAt < :end")
    List<AdminStatsView> findAllForAdminStatsByCreatedAtBetween(@Param("start") LocalDateTime start,
                                                                @Param("end") LocalDateTime end);

    /** 거주자 삭제 전 회원의 resident 참조를 null로 정리한다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.residentId = null where m.residentId = :residentId")
    int clearResidentReference(@Param("residentId") Long residentId);
}
