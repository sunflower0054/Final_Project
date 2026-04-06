package com.office.monitoring.resident;

import com.office.monitoring.aiSettings.AiSettings;
import com.office.monitoring.member.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResidentRegisterIntegrationTest extends ResidentIntegrationTestSupport {

    @Test
    void 거주자등록_성공시_거주자저장_회원연결_AI기본설정생성() throws Exception {
        mockMvc.perform(post("/api/v1/residents")
                        .with(user("new-user").roles("FAMILY"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                      "name": "  김영희  ",
                      "birthDate": "1943-05-12",
                      "address": "  서울시 강남구 테스트로 101  ",
                      "phone": "   ",
                      "disease": "  고혈압  ",
                      "latitude": 37.5665,
                      "longitude": 126.9780
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.residentId").isNumber())
                .andExpect(jsonPath("$.message").value("거주자 정보가 등록되었습니다."));

        Member member = memberRepository.findByUsername("new-user").orElseThrow();
        Resident resident = residentRepository.findById(member.getResidentId()).orElseThrow();
        AiSettings aiSettings = aiSettingsRepository.findByResidentId(resident.getId());

        assertThat(member.getResidentId()).isNotNull();
        assertThat(resident.getName()).isEqualTo("김영희");
        assertThat(resident.getBirthDate()).isEqualTo(LocalDate.of(1943, 5, 12));
        assertThat(resident.getAddress()).isEqualTo("서울시 강남구 테스트로 101");
        assertThat(resident.getPhone()).isNull();
        assertThat(resident.getDisease()).isEqualTo("고혈압");
        assertThat(aiSettings).isNotNull();
    }

    @Test
    void 이미_연결된_거주자가_있으면_거주자등록_실패() throws Exception {
        mockMvc.perform(post("/api/v1/residents")
                        .with(user("user").roles("FAMILY"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                      "name": "김영희",
                      "birthDate": "1943-05-12",
                      "address": "서울시 강남구 테스트로 101",
                      "phone": "010-5555-5555",
                      "disease": "고혈압",
                      "latitude": 37.5665,
                      "longitude": 126.9780
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 연결된 거주자 정보가 있습니다."));
    }

    @Test
    void 거주자등록_validation_실패시_400() throws Exception {
        mockMvc.perform(post("/api/v1/residents")
                        .with(user("new-user").roles("FAMILY"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                      "name": "",
                      "birthDate": "1943-05-12",
                      "address": "서울시 강남구 테스트로 101",
                      "phone": "010-5555-5555",
                      "disease": "고혈압",
                      "latitude": 37.5665,
                      "longitude": 126.9780
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이름은 필수입니다."));
    }
}
