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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
            .loginId("user")
            .password(passwordEncoder.encode("user1234!"))
            .name("테스트 사용자")
            .email("user@test.local")
            .role(Role.USER)
            .enabled(true)
            .build());

        memberRepository.save(Member.builder()
            .loginId("admin")
            .password(passwordEncoder.encode("admin1234!"))
            .name("테스트 관리자")
            .email("admin@test.local")
            .role(Role.ADMIN)
            .enabled(true)
            .build());
    }

    @Test
    void 비로그인_사용자_myinfo_접근시_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/myinfo"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/member/login"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void USER_권한_setting_접근시_403() throws Exception {
        mockMvc.perform(get("/setting"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void ADMIN_권한_setting_접근시_200() throws Exception {
        mockMvc.perform(get("/setting"))
            .andExpect(status().isOk());
    }

    @Test
    void 로그인_성공시_메인페이지로_이동() throws Exception {
        mockMvc.perform(formLogin("/member/login")
                .user("username", "user")
                .password("password", "user1234!"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void 로그인_실패시_error_파라미터로_이동() throws Exception {
        mockMvc.perform(formLogin("/member/login")
                .user("username", "user")
                .password("password", "wrong-password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/member/login?error"));
    }
}
