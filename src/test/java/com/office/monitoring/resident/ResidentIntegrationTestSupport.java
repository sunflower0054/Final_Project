package com.office.monitoring.resident;

import com.office.monitoring.aiSettings.AiSettingsRepository;
import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.member.Role;
import com.office.monitoring.member.WithdrawnUserRepository;
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
abstract class ResidentIntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected ResidentRepository residentRepository;

    @Autowired
    protected AiSettingsRepository aiSettingsRepository;

    @Autowired
    protected WithdrawnUserRepository withdrawnUserRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        aiSettingsRepository.deleteAll();
        residentRepository.deleteAll();
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

        memberRepository.save(Member.builder()
                .username("new-user")
                .password(passwordEncoder.encode("newuser1234!"))
                .name("신규 보호자")
                .phone("010-4444-4444")
                .birthYear(1995)
                .purpose("거주자 등록 테스트")
                .role(Role.FAMILY)
                .build());
    }
}
