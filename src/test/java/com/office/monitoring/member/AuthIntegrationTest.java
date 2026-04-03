package com.office.monitoring.member;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends MemberIntegrationTestSupport {

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

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("JSESSIONID", 0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void 인증없어도_auth_endpoint_접근가능() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .param("username", "missing-user")
                        .param("password", "wrong-password"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkUsername_중복아닌경우_available_true() throws Exception {
        mockMvc.perform(get("/api/v1/auth/check-username")
                        .param("username", "new-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 아이디입니다."));
    }

    @Test
    void checkUsername_중복인경우_available_false() throws Exception {
        mockMvc.perform(get("/api/v1/auth/check-username")
                        .param("username", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
    }

    @Test
    void 회원가입_성공시_회원저장과_BCrypt인코딩_검증() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                      "username": "new-user",
                      "password": "new-pass-1234!",
                      "name": "신규 사용자",
                      "phone": "010-9999-9999",
                      "purpose": "테스트 목적"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        Member saved = memberRepository.findByUsername("new-user").orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getRole()).isEqualTo(Role.FAMILY);
        assertThat(saved.getName()).isEqualTo("신규 사용자");
        assertThat(saved.getPhone()).isEqualTo("010-9999-9999");
        assertThat(saved.getPurpose()).isEqualTo("테스트 목적");
        assertThat(passwordEncoder.matches("new-pass-1234!", saved.getPassword())).isTrue();
        assertThat(saved.getPassword()).isNotEqualTo("new-pass-1234!");
    }

    @Test
    void 회원가입시_username_중복이면_실패() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                      "username": "user",
                      "password": "another-pass-1234!",
                      "name": "중복 사용자",
                      "phone": "010-1111-1111",
                      "purpose": "중복 테스트"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
    }

    @Test
    void 회원가입시_CSRF_없으면_403() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                    {
                      "username": "no-csrf-user",
                      "password": "pass-1234!",
                      "name": "무토큰 사용자",
                      "phone": "010-1234-5678",
                      "purpose": "csrf 검증"
                    }
                    """))
                .andExpect(status().isForbidden());
    }

    @Test
    void 로그인_API_성공시_HTTP세션_생성확인() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .param("username", "user")
                        .param("password", "user1234!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
    }
}
