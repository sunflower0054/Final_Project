package com.office.monitoring.admin;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 관리자 통계 화면의 접근 권한과 페이지 응답을 검증한다. */
class AdminPageAccessIntegrationTest extends AdminIntegrationTestSupport {

    @Test
    void 비로그인_사용자는_관리자통계_페이지에_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void FAMILY_권한은_관리자통계_페이지에서_403을_받는다() throws Exception {
        mockMvc.perform(get("/admin/stats")
                        .with(user("user").roles("FAMILY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void ADMIN은_관리자통계_페이지에_접근하고_필터가포함된_실제_API연결_스크립트를_받는다() throws Exception {
        mockMvc.perform(get("/admin/stats")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/v1/admin/stats/users")))
                .andExpect(content().string(containsString("/api/v1/admin/stats/residents")))
                .andExpect(content().string(containsString("/api/v1/admin/stats/events")))
                .andExpect(content().string(containsString("createdAtYearFilter")))
                .andExpect(content().string(containsString("createdAtMonthFilter")))
                .andExpect(content().string(containsString("resetCreatedAtFiltersBtn")))
                .andExpect(content().string(containsString("eventYearFilter")))
                .andExpect(content().string(containsString("eventMonthFilter")))
                .andExpect(content().string(containsString("eventTypeFilter")))
                .andExpect(content().string(containsString("eventStatusFilter")))
                .andExpect(content().string(containsString("buildUserStatsUrl")))
                .andExpect(content().string(containsString("buildResidentStatsUrl")))
                .andExpect(content().string(containsString("reloadCreatedAtStats")))
                .andExpect(content().string(containsString("buildEventStatsUrl")));
    }
}
