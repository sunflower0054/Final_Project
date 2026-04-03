package com.office.monitoring.member;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.residentId = null where m.residentId = :residentId")
    int clearResidentReference(@Param("residentId") Long residentId);
}
