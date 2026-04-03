package com.office.monitoring.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    List<Member> findByResidentId(Long residentId);

    List<Member> findAllByResidentId(Long residentId);
}