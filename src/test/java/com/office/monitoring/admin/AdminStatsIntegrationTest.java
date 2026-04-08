package com.office.monitoring.admin;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 관리자 통계 API의 접근 제어와 응답 구조를 검증한다. */
class AdminStatsIntegrationTest extends AdminIntegrationTestSupport {

    @Test
    void 비로그인_사용자는_관리자_회원통계_API에_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void FAMILY_권한은_관리자_회원통계_API에서_403을_받는다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats/users")
                        .with(user("user").roles("FAMILY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void ADMIN은_회원통계_API를_조회할_수_있다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats/users")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalUsers").value(2))
                .andExpect(jsonPath("$.ageGroups[0][0]").value("연령대"))
                .andExpect(jsonPath("$.ageGroups[3][1]").value(1))
                .andExpect(jsonPath("$.ageGroups[7][1]").value(1))
                .andExpect(jsonPath("$.purposes[0][0]").value("이용 목적"))
                .andExpect(jsonPath("$.purposes[1][0]").value("초기 목적"))
                .andExpect(jsonPath("$.purposes[1][1]").value(1))
                .andExpect(jsonPath("$.purposes[2][0]").value("미지정"))
                .andExpect(jsonPath("$.purposes[2][1]").value(1))
                .andExpect(jsonPath("$", hasKey("totalUsers")))
                .andExpect(jsonPath("$", hasKey("ageGroups")))
                .andExpect(jsonPath("$", hasKey("purposes")));
    }

    @Test
    void ADMIN은_거주자통계_API를_조회할_수_있다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats/residents")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalResidents").value(1))
                .andExpect(jsonPath("$.averageAge").value(84.0))
                .andExpect(jsonPath("$.ageGroups[0][0]").value("연령대"))
                .andExpect(jsonPath("$.ageGroups[3][1]").value(1))
                .andExpect(jsonPath("$.ageGroups[4][1]").value(0))
                .andExpect(jsonPath("$", hasKey("totalResidents")))
                .andExpect(jsonPath("$", hasKey("averageAge")))
                .andExpect(jsonPath("$", hasKey("ageGroups")));
    }

    @Test
    void ADMIN은_이벤트통계_API를_조회할_수_있다() throws Exception {
        eventRepository.deleteAll();
        saveEvent("FALL_DETECTED", "CONFIRMED", LocalDateTime.of(2025, 12, 15, 9, 0));
        saveEvent("NO_MOTION_DETECTED", "AUTO_REPORTED", LocalDateTime.of(2026, 1, 10, 9, 0));
        saveEvent("VIOLENT_MOTION_DETECTED", "CLOSED", LocalDateTime.of(2026, 1, 11, 9, 0));
        saveEvent("FALL_DETECTED", "PENDING", LocalDateTime.of(2026, 1, 12, 9, 0));

        mockMvc.perform(get("/api/v1/admin/stats/events")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalEvents").value(4))
                .andExpect(jsonPath("$.byType[0][0]").value("유형"))
                .andExpect(jsonPath("$.byType[1][1]").value(2))
                .andExpect(jsonPath("$.byType[2][1]").value(1))
                .andExpect(jsonPath("$.byType[3][1]").value(1))
                .andExpect(jsonPath("$.byStatus[0][0]").value("상태"))
                .andExpect(jsonPath("$.byStatus[1][1]").value(1))
                .andExpect(jsonPath("$.byStatus[2][1]").value(1))
                .andExpect(jsonPath("$.byStatus[3][1]").value(1))
                .andExpect(jsonPath("$.monthlyTrend[0][0]").value("월"))
                .andExpect(jsonPath("$.monthlyTrend[1][0]").value("2025-12"))
                .andExpect(jsonPath("$.monthlyTrend[1][1]").value(1))
                .andExpect(jsonPath("$.monthlyTrend[2][0]").value("2026-01"))
                .andExpect(jsonPath("$.monthlyTrend[2][1]").value(1))
                .andExpect(jsonPath("$.monthlyTrend[2][2]").value(1))
                .andExpect(jsonPath("$.monthlyTrend[2][3]").value(1))
                .andExpect(jsonPath("$", hasKey("totalEvents")))
                .andExpect(jsonPath("$", hasKey("byType")))
                .andExpect(jsonPath("$", hasKey("byStatus")))
                .andExpect(jsonPath("$", hasKey("monthlyTrend")));
    }

    @Test
    void ADMIN은_year만_있을때_해당연도_이벤트통계를_조회할_수_있다() throws Exception {
        eventRepository.deleteAll();
        saveEvent("FALL_DETECTED", "CONFIRMED", LocalDateTime.of(2025, 12, 15, 9, 0));
        saveEvent("NO_MOTION_DETECTED", "AUTO_REPORTED", LocalDateTime.of(2026, 3, 10, 9, 0));
        saveEvent("VIOLENT_MOTION_DETECTED", "CLOSED", LocalDateTime.of(2026, 4, 11, 9, 0));
        saveEvent("FALL_DETECTED", "PENDING", LocalDateTime.of(2026, 12, 1, 9, 0));

        mockMvc.perform(get("/api/v1/admin/stats/events")
                        .param("year", "2026")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalEvents").value(3))
                .andExpect(jsonPath("$.monthlyTrend.length()").value(13))
                .andExpect(jsonPath("$.monthlyTrend[1][0]").value("1월"))
                .andExpect(jsonPath("$.monthlyTrend[3][0]").value("3월"))
                .andExpect(jsonPath("$.monthlyTrend[3][2]").value(1))
                .andExpect(jsonPath("$.monthlyTrend[4][0]").value("4월"))
                .andExpect(jsonPath("$.monthlyTrend[4][3]").value(1))
                .andExpect(jsonPath("$.monthlyTrend[12][0]").value("12월"))
                .andExpect(jsonPath("$.monthlyTrend[12][1]").value(1));
    }

    @Test
    void ADMIN은_year와_month가_있을때_해당월_이벤트통계를_조회할_수_있다() throws Exception {
        eventRepository.deleteAll();
        saveEvent("FALL_DETECTED", "CONFIRMED", LocalDateTime.of(2026, 4, 8, 9, 0));
        saveEvent("NO_MOTION_DETECTED", "PENDING", LocalDateTime.of(2026, 4, 9, 9, 0));
        saveEvent("VIOLENT_MOTION_DETECTED", "CLOSED", LocalDateTime.of(2026, 5, 1, 9, 0));

        mockMvc.perform(get("/api/v1/admin/stats/events")
                        .param("year", "2026")
                        .param("month", "4")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalEvents").value(2))
                .andExpect(jsonPath("$.byType[1][1]").value(1))
                .andExpect(jsonPath("$.byType[2][1]").value(1))
                .andExpect(jsonPath("$.byType[3][1]").value(0))
                .andExpect(jsonPath("$.byStatus[1][1]").value(1))
                .andExpect(jsonPath("$.byStatus[2][1]").value(0))
                .andExpect(jsonPath("$.byStatus[3][1]").value(0))
                .andExpect(jsonPath("$.monthlyTrend.length()").value(2))
                .andExpect(jsonPath("$.monthlyTrend[1][0]").value("4월"))
                .andExpect(jsonPath("$.monthlyTrend[1][1]").value(1))
                .andExpect(jsonPath("$.monthlyTrend[1][2]").value(1))
                .andExpect(jsonPath("$.monthlyTrend[1][3]").value(0));
    }

    @Test
    void ADMIN은_month만_있을때_전체기준으로_안전하게_이벤트통계를_조회할_수_있다() throws Exception {
        eventRepository.deleteAll();
        saveEvent("FALL_DETECTED", "CONFIRMED", LocalDateTime.of(2025, 12, 15, 9, 0));
        saveEvent("VIOLENT_MOTION_DETECTED", "AUTO_REPORTED", LocalDateTime.of(2026, 1, 10, 9, 0));

        mockMvc.perform(get("/api/v1/admin/stats/events")
                        .param("month", "4")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalEvents").value(2))
                .andExpect(jsonPath("$.monthlyTrend.length()").value(3))
                .andExpect(jsonPath("$.monthlyTrend[1][0]").value("2025-12"))
                .andExpect(jsonPath("$.monthlyTrend[2][0]").value("2026-01"));
    }
}
