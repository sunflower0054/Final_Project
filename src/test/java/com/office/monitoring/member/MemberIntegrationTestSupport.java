package com.office.monitoring.member;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
/** MemberIntegrationTestSupport 테스트를 정의한다. */
abstract class MemberIntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected WithdrawnUserRepository withdrawnUserRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
/** setUp 시나리오를 검증한다. */
    void setUp() {
        memberRepository.deleteAll();
        withdrawnUserRepository.deleteAll();

        memberRepository.save(Member.builder()
                .username("user")
                .password(passwordEncoder.encode("user1234!"))
                .name("테스트 사용자")
                .phone("010-2222-2222")
                .birthYear(1990)
                .purpose("초기 목적")
                .residentId(101L)
                .role(Role.FAMILY)
                .build());

        memberRepository.save(Member.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin1234!"))
                .name("테스트 관리자")
                .phone("010-3333-3333")
                .role(Role.ADMIN)
                .build());
    }
}
