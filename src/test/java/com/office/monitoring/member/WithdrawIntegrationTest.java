package com.office.monitoring.member;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WithdrawIntegrationTest extends MemberIntegrationTestSupport {

    @Test
    void 비로그인_상태_회원탈퇴_API_접근시_차단() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void 로그인_상태_회원탈퇴_성공() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .with(user("user").roles("FAMILY"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원탈퇴가 완료되었습니다."));
    }

    @Test
    void 회원탈퇴_성공시_users_테이블에서_삭제됨() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .with(user("user").roles("FAMILY"))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(memberRepository.findByUsername("user")).isEmpty();
    }

    @Test
    void 회원탈퇴_성공시_withdrawn_users_테이블에_복사됨() throws Exception {
        Member beforeWithdraw = memberRepository.findByUsername("user").orElseThrow();

        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .with(user("user").roles("FAMILY"))
                        .with(csrf()))
                .andExpect(status().isOk());

        WithdrawnUser withdrawnUser = withdrawnUserRepository.findAll().stream()
                .max(Comparator.comparing(WithdrawnUser::getId))
                .orElseThrow();

        assertThat(withdrawnUser.getOriginalUserId()).isEqualTo(beforeWithdraw.getId());
        assertThat(withdrawnUser.getUsername()).isEqualTo(beforeWithdraw.getUsername());
        assertThat(withdrawnUser.getPassword()).isEqualTo(beforeWithdraw.getPassword());
        assertThat(withdrawnUser.getName()).isEqualTo(beforeWithdraw.getName());
        assertThat(withdrawnUser.getPhone()).isEqualTo(beforeWithdraw.getPhone());
        assertThat(withdrawnUser.getRole()).isEqualTo(beforeWithdraw.getRole());
        assertThat(withdrawnUser.getBirthYear()).isEqualTo(beforeWithdraw.getBirthYear());
        assertThat(withdrawnUser.getPurpose()).isEqualTo(beforeWithdraw.getPurpose());
        assertThat(withdrawnUser.getResidentId()).isEqualTo(beforeWithdraw.getResidentId());
        assertThat(withdrawnUser.getCreatedAt()).isEqualTo(beforeWithdraw.getCreatedAt());
        assertThat(withdrawnUser.getWithdrawnAt()).isNotNull();
    }

    @Test
    void 회원탈퇴_성공시_세션_무효화() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .param("username", "user")
                        .param("password", "user1234!"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(session.isInvalid()).isTrue();

        mockMvc.perform(get("/api/v1/my-info").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }
}
