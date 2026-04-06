package com.office.monitoring.resident;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** ResidentAdminIntegrationTest 테스트를 정의한다. */
class ResidentAdminIntegrationTest extends ResidentIntegrationTestSupport {

/** ADMIN은_임의의_거주자정보를_수정할_수_있다 시나리오를 검증한다. */
    @Test
    void ADMIN은_임의의_거주자정보를_수정할_수_있다() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("수정 전")
                .birthDate(LocalDate.of(1940, 1, 1))
                .address("수정 전 주소")
                .phone("010-1111-1111")
                .disease("고혈압")
                .latitude(37.11)
                .longitude(127.11)
                .build());

        mockMvc.perform(put("/api/v1/residents/{id}", resident.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                      "name": "  관리자 수정  ",
                      "birthDate": "1940-02-02",
                      "address": "  관리자 수정 주소  ",
                      "phone": "  010-9999-8888  ",
                      "disease": "  관절염  ",
                      "latitude": 36.1234,
                      "longitude": 128.5678
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("거주자 정보가 수정되었습니다."));

        Resident updated = residentRepository.findById(resident.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("관리자 수정");
        assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1940, 2, 2));
        assertThat(updated.getAddress()).isEqualTo("관리자 수정 주소");
        assertThat(updated.getPhone()).isEqualTo("010-9999-8888");
        assertThat(updated.getDisease()).isEqualTo("관절염");
        assertThat(updated.getLatitude()).isEqualTo(36.1234);
        assertThat(updated.getLongitude()).isEqualTo(128.5678);
    }

/** ADMIN은_연결관계와_무관하게_이력없는_거주자를_삭제할_수_있다 시나리오를 검증한다. */
    @Test
    void ADMIN은_연결관계와_무관하게_이력없는_거주자를_삭제할_수_있다() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("삭제 대상")
                .birthDate(LocalDate.of(1941, 3, 3))
                .address("삭제 주소")
                .phone("010-2222-3333")
                .disease("없음")
                .latitude(37.22)
                .longitude(127.22)
                .build());

        mockMvc.perform(delete("/api/v1/residents/{id}", resident.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("거주자 정보가 삭제되었습니다."));

        assertThat(residentRepository.findById(resident.getId())).isEmpty();
    }
}
