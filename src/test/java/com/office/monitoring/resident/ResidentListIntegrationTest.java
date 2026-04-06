package com.office.monitoring.resident;

import com.office.monitoring.member.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** ResidentListIntegrationTest 테스트를 정의한다. */
class ResidentListIntegrationTest extends ResidentIntegrationTestSupport {

/** FAMILY_연결된_거주자가_있으면_본인_거주자만_목록으로_조회한다 시나리오를 검증한다. */
    @Test
    void FAMILY_연결된_거주자가_있으면_본인_거주자만_목록으로_조회한다() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("박순자")
                .birthDate(LocalDate.of(1940, 2, 14))
                .address("서울시 종로구 어딘가")
                .phone("010-1111-2222")
                .disease("당뇨")
                .latitude(37.57)
                .longitude(126.98)
                .build());

        Member member = memberRepository.findByUsername("new-user").orElseThrow();
        member.assignResident(resident.getId());
        memberRepository.save(member);

        mockMvc.perform(get("/api/v1/residents")
                        .with(user("new-user").roles("FAMILY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.residents.length()").value(1))
                .andExpect(jsonPath("$.residents[0].id").value(resident.getId()))
                .andExpect(jsonPath("$.residents[0].name").value("박순자"));
    }

/** FAMILY_연결된_거주자가_없으면_빈_목록을_반환한다 시나리오를 검증한다. */
    @Test
    void FAMILY_연결된_거주자가_없으면_빈_목록을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/residents")
                        .with(user("new-user").roles("FAMILY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.residents.length()").value(0));
    }

/** FAMILY_회원에_연결ID만_남고_실제_거주자가_없어도_빈_목록을_반환한다 시나리오를 검증한다. */
    @Test
    void FAMILY_회원에_연결ID만_남고_실제_거주자가_없어도_빈_목록을_반환한다() throws Exception {
        mockMvc.perform(get("/api/v1/residents")
                        .with(user("user").roles("FAMILY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.residents.length()").value(0));
    }

/** ADMIN은_전체_거주자_목록을_ID오름차순으로_조회한다 시나리오를 검증한다. */
    @Test
    void ADMIN은_전체_거주자_목록을_ID오름차순으로_조회한다() throws Exception {
        Resident first = residentRepository.save(Resident.builder()
                .name("첫 번째")
                .birthDate(LocalDate.of(1938, 1, 1))
                .address("서울시 중구")
                .phone("010-1000-1000")
                .disease("관절염")
                .latitude(37.55)
                .longitude(126.99)
                .build());

        Resident second = residentRepository.save(Resident.builder()
                .name("두 번째")
                .birthDate(LocalDate.of(1942, 2, 2))
                .address("서울시 서초구")
                .phone("010-2000-2000")
                .disease("고혈압")
                .latitude(37.49)
                .longitude(127.01)
                .build());

        mockMvc.perform(get("/api/v1/residents")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.residents.length()").value(2))
                .andExpect(jsonPath("$.residents[0].id").value(first.getId()))
                .andExpect(jsonPath("$.residents[0].name").value("첫 번째"))
                .andExpect(jsonPath("$.residents[1].id").value(second.getId()))
                .andExpect(jsonPath("$.residents[1].name").value("두 번째"));
    }
}
