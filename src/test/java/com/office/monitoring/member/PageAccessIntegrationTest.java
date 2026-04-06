package com.office.monitoring.member;

import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 회원 기능의 조건별 응답과 저장 결과를 검증하는 테스트 클래스. */
class PageAccessIntegrationTest extends MemberIntegrationTestSupport {

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void 비로그인_사용자_myinfo_접근시_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/myinfo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void FAMILY_로그인_setting_접근시_200() throws Exception {
        mockMvc.perform(get("/setting/setting").with(user("user").roles("FAMILY")))
                .andExpect(status().isOk());
    }

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void ADMIN_권한_setting_접근시_200() throws Exception {
        mockMvc.perform(get("/setting/setting").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void 비로그인_사용자_setting_접근시_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/setting/setting"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void 인증없으면_보호페이지는_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/camera"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }
}
