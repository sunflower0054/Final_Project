package com.office.monitoring.resident;

import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ResidentPageAccessIntegrationTest extends ResidentIntegrationTestSupport {

    @Test
    void 비로그인_사용자_resident_detail_접근시_로그인페이지로_리다이렉트() throws Exception {
        mockMvc.perform(get("/resident/detail"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void 로그인_사용자_resident_detail_접근시_200() throws Exception {
        mockMvc.perform(get("/resident/detail").with(user("user").roles("FAMILY")))
                .andExpect(status().isOk())
                .andExpect(view().name("resident/resident_detail"));
    }

    @Test
    void 로그인_사용자_resident_edit_접근시_200() throws Exception {
        mockMvc.perform(get("/resident/edit").with(user("user").roles("FAMILY")))
                .andExpect(status().isOk())
                .andExpect(view().name("resident/resident_edit"));
    }
}
