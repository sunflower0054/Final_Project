package com.office.monitoring.admin;

import com.office.monitoring.event.Event;
import com.office.monitoring.event.EventRepository;
import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.member.Role;
import com.office.monitoring.resident.Resident;
import com.office.monitoring.resident.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
/** 관리자 통계 통합 테스트에서 공통 기준 데이터를 제공한다. */
abstract class AdminIntegrationTestSupport {

    protected Long defaultResidentId;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected ResidentRepository residentRepository;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();

        eventRepository.deleteAll();
        residentRepository.deleteAll();
        memberRepository.deleteAll();

        memberRepository.save(Member.builder()
                .username("user")
                .password(passwordEncoder.encode("user1234!"))
                .name("테스트 사용자")
                .phone("010-2222-2222")
                .birthYear(today.getYear() - 36)
                .purpose("초기 목적")
                .role(Role.FAMILY)
                .build());

        memberRepository.save(Member.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin1234!"))
                .name("테스트 관리자")
                .phone("010-3333-3333")
                .role(Role.ADMIN)
                .build());

        Resident resident = residentRepository.save(Resident.builder()
                .name("통계 거주자")
                .birthDate(today.minusYears(84))
                .address("서울시 종로구")
                .phone("010-5555-5555")
                .disease("고혈압")
                .latitude(37.57)
                .longitude(126.98)
                .build());
        defaultResidentId = resident.getId();

        eventRepository.save(Event.builder()
                .residentId(defaultResidentId)
                .eventType("FALL_DETECTED")
                .timestamp(LocalDateTime.of(2026, 4, 8, 9, 0))
                .status("CLOSED")
                .build());
    }

    protected Event saveEvent(String eventType, String status, LocalDateTime timestamp) {
        return eventRepository.save(Event.builder()
                .residentId(defaultResidentId)
                .eventType(eventType)
                .timestamp(timestamp)
                .status(status)
                .build());
    }

    protected Member saveMemberForStats(String username,
                                        Integer birthYear,
                                        String purpose,
                                        LocalDateTime createdAt) {
        return memberRepository.save(Member.builder()
                .username(username)
                .password(passwordEncoder.encode("user1234!"))
                .name(username)
                .phone("010-9999-9999")
                .birthYear(birthYear)
                .purpose(purpose)
                .role(Role.FAMILY)
                .createdAt(createdAt)
                .build());
    }

    protected Resident saveResidentForStats(String name,
                                            LocalDate birthDate,
                                            LocalDateTime createdAt) {
        return residentRepository.save(Resident.builder()
                .name(name)
                .birthDate(birthDate)
                .address("Seoul")
                .phone("010-8888-8888")
                .disease("DISEASE")
                .latitude(37.57)
                .longitude(126.98)
                .createdAt(createdAt)
                .build());
    }
}
