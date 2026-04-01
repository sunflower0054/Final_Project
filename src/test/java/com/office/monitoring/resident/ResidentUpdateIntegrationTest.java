package com.office.monitoring.resident;

import com.office.monitoring.member.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResidentUpdateIntegrationTest extends ResidentIntegrationTestSupport {

    @Test
    void 연결된_거주자수정_성공시_DB반영() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("수정 전")
                .birthDate(LocalDate.of(1941, 6, 1))
                .address("수정 전 주소")
                .phone("010-1212-3434")
                .disease("수정 전 질환")
                .latitude(37.11)
                .longitude(127.11)
                .build());

        Member member = memberRepository.findByUsername("user").orElseThrow();
        member.assignResident(resident.getId());
        memberRepository.save(member);

        mockMvc.perform(put("/api/v1/residents/{id}", resident.getId())
                        .with(user("user").roles("FAMILY"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                      "name": "  수정 후  ",
                      "birthDate": "1941-07-15",
                      "address": "  수정 후 주소  ",
                      "phone": "   ",
                      "disease": "  치매 초기  ",
                      "latitude": 35.1234,
                      "longitude": 128.5678
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("거주자 정보가 수정되었습니다."));

        Resident updated = residentRepository.findById(resident.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("수정 후");
        assertThat(updated.getPhone()).isNull();
        assertThat(updated.getDisease()).isEqualTo("치매 초기");
    }

    @Test
    void 연결되지않은_거주자수정시_403() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("남의 거주자")
                .birthDate(LocalDate.of(1942, 8, 8))
                .address("서울시 어딘가")
                .phone("010-5555-6666")
                .disease("없음")
                .latitude(37.20)
                .longitude(127.20)
                .build());

        mockMvc.perform(put("/api/v1/residents/{id}", resident.getId())
                        .with(user("user").roles("FAMILY"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                      "name": "수정 시도",
                      "birthDate": "1942-08-08",
                      "address": "수정 시도 주소",
                      "phone": "010-7777-8888",
                      "disease": "없음",
                      "latitude": 37.21,
                      "longitude": 127.21
                    }
                    """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("해당 거주자 정보에 접근할 수 없습니다."));
    }

    @Test
    void 거주자수정_validation_실패시_400() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("원본")
                .birthDate(LocalDate.of(1941, 1, 1))
                .address("원본 주소")
                .phone("010-0000-0000")
                .disease("원본 질환")
                .latitude(37.30)
                .longitude(127.30)
                .build());

        Member member = memberRepository.findByUsername("user").orElseThrow();
        member.assignResident(resident.getId());
        memberRepository.save(member);

        mockMvc.perform(put("/api/v1/residents/{id}", resident.getId())
                        .with(user("user").roles("FAMILY"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                      "name": "",
                      "birthDate": "1941-01-01",
                      "address": "원본 주소",
                      "phone": "010-0000-0000",
                      "disease": "원본 질환",
                      "latitude": 37.30,
                      "longitude": 127.30
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이름은 필수입니다."));
    }
}
