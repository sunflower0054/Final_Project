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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    }

    @Test
    void 회원조회는_username으로_동작한다() {
        Member found = memberRepository.findByUsername("user").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("user");
        assertThat(found.getPhone()).isEqualTo("010-2222-2222");
    }

    @Test
    void 비로그인_사용자_myinfo_접근시_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/myinfo"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void FAMILY_로그인_setting_접근시_200() throws Exception {
        mockMvc.perform(get("/setting").with(user("user").roles("FAMILY")))
            .andExpect(status().isOk());
    }

    @Test
    void ADMIN_권한_setting_접근시_200() throws Exception {
        mockMvc.perform(get("/setting").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk());
    }

    @Test
    void 비로그인_사용자_setting_접근시_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/setting"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/member/login"));
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
    void 로그인_API_성공시_HTTP세션_생성확인() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .param("username", "user")
                .param("password", "user1234!"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
    }

    @Test
    void 비로그인_상태_GET_myinfo_API_접근시_인증진입점_리다이렉트() throws Exception {
        mockMvc.perform(get("/api/v1/my-info"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/member/login"));
    }

    @Test
    void 로그인_상태_GET_myinfo_API_성공_응답필드_검증() throws Exception {
        mockMvc.perform(get("/api/v1/my-info").with(user("user").roles("FAMILY")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.username").value("user"))
            .andExpect(jsonPath("$.name").value("테스트 사용자"))
            .andExpect(jsonPath("$.phone").value("010-2222-2222"))
            .andExpect(jsonPath("$.purpose").value("초기 목적"))
            .andExpect(jsonPath("$.role").value("FAMILY"));
    }

    @Test
    void 로그인_상태_PUT_myinfo_API_성공시_DB반영과_응답필드_검증() throws Exception {
        mockMvc.perform(put("/api/v1/my-info")
                .with(user("user").roles("FAMILY"))
                .with(csrf())
                .contentType("application/json")
                .content("""
                    {
                      "name": "수정 사용자",
                      "phone": "010-7777-7777",
                      "purpose": "가족 모니터링"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.username").value("user"))
            .andExpect(jsonPath("$.name").value("수정 사용자"))
            .andExpect(jsonPath("$.phone").value("010-7777-7777"))
            .andExpect(jsonPath("$.purpose").value("가족 모니터링"))
            .andExpect(jsonPath("$.role").value("FAMILY"));

        Member updated = memberRepository.findByUsername("user").orElseThrow();
        assertThat(updated.getName()).isEqualTo("수정 사용자");
        assertThat(updated.getPhone()).isEqualTo("010-7777-7777");
        assertThat(updated.getPurpose()).isEqualTo("가족 모니터링");
    }

    @Test
    void 로그인_상태_PUT_myinfo_API_validation_실패시_400과_기존예외응답형식_검증() throws Exception {
        mockMvc.perform(put("/api/v1/my-info")
                .with(user("user").roles("FAMILY"))
                .with(csrf())
                .contentType("application/json")
                .content("""
                    {
                      "name": "",
                      "phone": "010-8888-8888",
                      "purpose": "검증 실패"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$", hasKey("success")))
            .andExpect(jsonPath("$", hasKey("message")));
    }
}
