package com.office.monitoring.member;

import org.springframework.data.jpa.repository.JpaRepository;

/** WithdrawnUserRepository의 역할을 담당한다. */
public interface WithdrawnUserRepository extends JpaRepository<WithdrawnUser, Long> {
}
