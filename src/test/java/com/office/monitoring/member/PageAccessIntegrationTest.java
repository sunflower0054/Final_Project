package com.office.monitoring.member;

import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PageAccessIntegrationTest extends MemberIntegrationTestSupport {

    @Test
    void 비로그인_사용자_myinfo_접근시_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/myinfo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void FAMILY_로그인_setting_접근시_200() throws Exception {
        mockMvc.perform(get("/setting/setting").with(user("user").roles("FAMILY")))
                .andExpect(status().isOk());
    }

    @Test
    void ADMIN_권한_setting_접근시_200() throws Exception {
        mockMvc.perform(get("/setting/setting").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void 비로그인_사용자_setting_접근시_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/setting/setting"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void 인증없으면_보호페이지는_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/camera"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }
}
