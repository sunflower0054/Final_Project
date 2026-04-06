package com.office.monitoring.aiSettings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiSettingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiSettingsRepository aiSettingsRepository;


    @BeforeEach
    void setUp() {
        aiSettingsRepository.deleteAll();
    }

    @Test
    void 인증된_FAMILY사용자가_GET호출하면_설정값을_정상반환한다() throws Exception {
        aiSettingsRepository.save(AiSettings.builder()
                .residentId(1L)
                .fallSensitivity(0.2)
                .noMotionThreshold(1500)
                .velocityThreshold(0.12)
                .updatedAt(LocalDateTime.of(2026, 4, 6, 1, 2, 3))
                .build());

        mockMvc.perform(get("/api/v1/settings")
                        .with(user("user").roles("FAMILY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallSensitivity").value(0.2))
                .andExpect(jsonPath("$.noMotionThreshold").value(1500))
                .andExpect(jsonPath("$.velocityThreshold").value(0.12))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void residentId1설정이_없을때_GET호출하면_현재구현대로_예외가_전파된다() {
        assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/settings")
                        .with(user("user").roles("FAMILY"))))
                .hasRootCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("AI 설정이 존재하지 않습니다.");
    }

    @Test
    void 인증된_사용자가_PUT호출하면_설정값이_저장되고_updatedAt이_채워진다() throws Exception {
        mockMvc.perform(put("/api/v1/settings")
                        .with(user("user").roles("FAMILY"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "fallSensitivity": 0.31,
                                  "noMotionThreshold": 2100,
                                  "velocityThreshold": 0.22
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallSensitivity").value(0.31))
                .andExpect(jsonPath("$.noMotionThreshold").value(2100))
                .andExpect(jsonPath("$.velocityThreshold").value(0.22))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        AiSettings saved = aiSettingsRepository.findByResidentId(1L).orElseThrow();
        assertThat(saved.getFallSensitivity()).isEqualTo(0.31);
        assertThat(saved.getNoMotionThreshold()).isEqualTo(2100);
        assertThat(saved.getVelocityThreshold()).isEqualTo(0.22);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void 기존설정이_있을때_PUT호출하면_새행추가가아닌_업데이트로_동작한다() throws Exception {
        AiSettings existing = aiSettingsRepository.save(AiSettings.builder()
                .residentId(1L)
                .fallSensitivity(0.11)
                .noMotionThreshold(1000)
                .velocityThreshold(0.09)
                .updatedAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                .build());

        mockMvc.perform(put("/api/v1/settings")
                        .with(user("user").roles("FAMILY"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "fallSensitivity": 0.44,
                                  "noMotionThreshold": 3200,
                                  "velocityThreshold": 0.35
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallSensitivity").value(0.44))
                .andExpect(jsonPath("$.noMotionThreshold").value(3200))
                .andExpect(jsonPath("$.velocityThreshold").value(0.35));

        long count = aiSettingsRepository.findAll().stream()
                .filter(settings -> settings.getResidentId().equals(1L))
                .count();
        AiSettings updated = aiSettingsRepository.findByResidentId(1L).orElseThrow();

        assertThat(count).isEqualTo(1L);
        assertThat(updated.getId()).isEqualTo(existing.getId());
        assertThat(updated.getFallSensitivity()).isEqualTo(0.44);
        assertThat(updated.getNoMotionThreshold()).isEqualTo(3200);
        assertThat(updated.getVelocityThreshold()).isEqualTo(0.35);
    }

    @Test
    void 인증된_사용자가_apply호출하면_FastAPI없어도_200을_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/settings/apply")
                        .with(user("user").roles("FAMILY"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "fallSensitivity": 0.55,
                                  "noMotionThreshold": 1800,
                                  "velocityThreshold": 0.25
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void 인증없이_GET요청하면_로그인페이지로_리다이렉트된다() throws Exception {
        mockMvc.perform(get("/api/v1/settings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void 인증없이_PUT요청하면_로그인페이지로_리다이렉트된다() throws Exception {
        mockMvc.perform(put("/api/v1/settings")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "fallSensitivity": 0.3,
                                  "noMotionThreshold": 1200,
                                  "velocityThreshold": 0.2
                                }
                                """))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    void PUT요청에서_csrf없이_요청하면_403이다() throws Exception {
        mockMvc.perform(put("/api/v1/settings")
                        .with(user("user").roles("FAMILY"))
                        .contentType("application/json")
                        .content("""
                                {
                                  "fallSensitivity": 0.3,
                                  "noMotionThreshold": 1200,
                                  "velocityThreshold": 0.2
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void apply_POST요청에서_csrf없이_요청하면_403이다() throws Exception {
        mockMvc.perform(post("/api/v1/settings/apply")
                        .with(user("user").roles("FAMILY"))
                        .contentType("application/json")
                        .content("""
                                {
                                  "fallSensitivity": 0.3,
                                  "noMotionThreshold": 1200,
                                  "velocityThreshold": 0.2
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
