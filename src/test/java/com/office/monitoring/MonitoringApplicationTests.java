package com.office.monitoring;

import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.member.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MonitoringApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        memberRepository.save(Member.builder()
            .username("user")
            .password(passwordEncoder.encode("user1234!"))
            .name("테스트 사용자")
            .phone("010-2222-2222")
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

    @Test
    void 비로그인_사용자_myinfo_접근시_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/myinfo"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void FAMILY_권한_setting_접근시_403() throws Exception {
        mockMvc.perform(get("/setting").with(user("user").roles("FAMILY")))
            .andExpect(status().isForbidden());
    }

    @Test
    void ADMIN_권한_setting_접근시_200() throws Exception {
        mockMvc.perform(get("/setting").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk());
    }

    @Test
    void 로그인_API_성공시_세션생성_및_JSON응답() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .param("username", "user")
                .param("password", "user1234!"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("JSESSIONID"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.username").value("user"))
            .andExpect(jsonPath("$.name").value("테스트 사용자"))
            .andExpect(jsonPath("$.role").value("FAMILY"))
            .andExpect(jsonPath("$.token").value(nullValue()))
            .andExpect(jsonPath("$.message").value("로그인 성공"));
    }

    @Test
    void 로그인_API_실패시_401_JSON응답() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .param("username", "user")
                .param("password", "wrong-password"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 로그아웃_API_호출시_성공_JSON응답() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .param("username", "user")
                .param("password", "user1234!"))
            .andExpect(status().isOk())
            .andReturn();

        mockMvc.perform(post("/api/v1/auth/logout").session((org.springframework.mock.web.MockHttpSession) loginResult.getRequest().getSession(false)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));
    }

    @Test
    void 인증없어도_auth_endpoint_접근가능() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .param("username", "missing-user")
                .param("password", "wrong-password"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 인증없으면_보호페이지는_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/camera"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/member/login"));
    }
}
