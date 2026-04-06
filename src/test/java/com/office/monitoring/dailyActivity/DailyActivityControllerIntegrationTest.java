package com.office.monitoring.dailyActivity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DailyActivityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
                create table if not exists daily_activity (
                    id bigint auto_increment primary key,
                    resident_id bigint not null,
                    date date not null,
                    motion_score int not null,
                    constraint uk_daily_activity_resident_date unique (resident_id, date)
                )
                """);
        jdbcTemplate.execute("delete from daily_activity");
    }

    @Test
    void 정상요청이면_success를_반환하고_DB에_저장된다() throws Exception {
        mockMvc.perform(post("/api/v1/daily-activity")
                        .param("resident_id", "11")
                        .param("date", "2026-04-06")
                        .param("motion_score", "1234"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        Integer count = jdbcTemplate.queryForObject("select count(*) from daily_activity", Integer.class);
        Long residentId = jdbcTemplate.queryForObject("select resident_id from daily_activity limit 1", Long.class);
        Date date = jdbcTemplate.queryForObject("select date from daily_activity limit 1", Date.class);
        Integer motionScore = jdbcTemplate.queryForObject("select motion_score from daily_activity limit 1", Integer.class);

        assertThat(count).isEqualTo(1);
        assertThat(residentId).isEqualTo(11L);
        assertThat(date).isEqualTo(Date.valueOf(LocalDate.of(2026, 4, 6)));
        assertThat(motionScore).isEqualTo(1234);
    }

    @Test
    void 같은_거주자와_날짜로_두번호출하면_한건만_유지되고_motionScore가_업데이트된다() throws Exception {
        mockMvc.perform(post("/api/v1/daily-activity")
                        .param("resident_id", "22")
                        .param("date", "2026-04-05")
                        .param("motion_score", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        mockMvc.perform(post("/api/v1/daily-activity")
                        .param("resident_id", "22")
                        .param("date", "2026-04-05")
                        .param("motion_score", "999"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        Integer count = jdbcTemplate.queryForObject("select count(*) from daily_activity", Integer.class);
        Integer motionScore = jdbcTemplate.queryForObject(
                "select motion_score from daily_activity where resident_id = ? and date = ?",
                Integer.class,
                22L,
                Date.valueOf(LocalDate.of(2026, 4, 5))
        );

        assertThat(count).isEqualTo(1);
        assertThat(motionScore).isEqualTo(999);
    }

    @Test
    void residentId가_숫자가_아니면_500과_저장실패를_반환하고_DB저장이_없다() throws Exception {
        mockMvc.perform(post("/api/v1/daily-activity")
                        .param("resident_id", "abc")
                        .param("date", "2026-04-06")
                        .param("motion_score", "300"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("저장 실패"));

        Integer count = jdbcTemplate.queryForObject("select count(*) from daily_activity", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    void date형식이_잘못되면_500을_반환하고_DB저장이_없다() throws Exception {
        mockMvc.perform(post("/api/v1/daily-activity")
                        .param("resident_id", "33")
                        .param("date", "2026/04/06")
                        .param("motion_score", "300"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("저장 실패"));

        Integer count = jdbcTemplate.queryForObject("select count(*) from daily_activity", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    void motionScore가_숫자가_아니면_500을_반환하고_DB저장이_없다() throws Exception {
        mockMvc.perform(post("/api/v1/daily-activity")
                        .param("resident_id", "44")
                        .param("date", "2026-04-06")
                        .param("motion_score", "high"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("저장 실패"));

        Integer count = jdbcTemplate.queryForObject("select count(*) from daily_activity", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    void 인증없이도_호출할수있다() throws Exception {
        mockMvc.perform(post("/api/v1/daily-activity")
                        .param("resident_id", "55")
                        .param("date", "2026-04-06")
                        .param("motion_score", "500"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }

    @Test
    void csrf없이도_호출할수있다() throws Exception {
        mockMvc.perform(post("/api/v1/daily-activity")
                        .param("resident_id", "66")
                        .param("date", "2026-04-06")
                        .param("motion_score", "600"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));
    }
}
