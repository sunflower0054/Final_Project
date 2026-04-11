package com.office.monitoring.resident;

import com.office.monitoring.aiSettings.AiSettingsRepository;
import com.office.monitoring.event.EventRepository;
import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.member.Role;
import com.office.monitoring.member.WithdrawnUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
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
    protected EventRepository eventRepository;

    @Autowired
    protected WithdrawnUserRepository withdrawnUserRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    /** 요청된 거주자 작업의 입력 조건을 반영해 결과를 만든다. */
    void setUp() {
        eventRepository.deleteAll();
        aiSettingsRepository.deleteAll();
        clearDailyActivityTableIfExists();
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

    /** 요청 데이터를 거주자 기준으로 저장하고 저장 결과를 반환한다. */
    protected void createDailyActivityTableIfNeeded() {
        jdbcTemplate.execute("""
                create table if not exists daily_activity (
                    id bigint auto_increment primary key,
                    resident_id bigint not null,
                    date date not null,
                    motion_score int not null
                )
                """);
    }

    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    private void clearDailyActivityTableIfExists() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'DAILY_ACTIVITY'",
                Integer.class
        );

        if (tableCount != null && tableCount > 0) {
            jdbcTemplate.execute("delete from daily_activity");
        }
    }
}
