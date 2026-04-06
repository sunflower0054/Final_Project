package com.office.monitoring.resident;

import org.springframework.data.jpa.repository.JpaRepository;

/** ResidentRepository의 역할을 담당한다. */
public interface ResidentRepository extends JpaRepository<Resident, Long> {
}
