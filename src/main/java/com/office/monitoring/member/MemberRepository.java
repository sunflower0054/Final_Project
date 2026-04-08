package com.office.monitoring.member;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** 회원 정보 조회, 가입, 수정, 탈퇴와 관련된 도메인 동작을 담당하는 구성 요소. */
public interface MemberRepository extends JpaRepository<Member, Long> {

    interface AdminStatsView {
        Integer getBirthYear();
        String getPurpose();
    }

    Optional<Member> findByUsername(String username);

    List<Member> findByResidentId(Long residentId);

    List<Member> findAllByResidentId(Long residentId);

    @Query("select m.birthYear as birthYear, m.purpose as purpose from Member m")
    List<AdminStatsView> findAllForAdminStats();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.residentId = null where m.residentId = :residentId")
    int clearResidentReference(@Param("residentId") Long residentId);
}
