package com.office.monitoring.member;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MyInfoIntegrationTest extends MemberIntegrationTestSupport {

    @Test
    void 비로그인_상태_GET_myinfo_API_접근시_인증진입점_리다이렉트() throws Exception {
        mockMvc.perform(get("/api/v1/my-info"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
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
