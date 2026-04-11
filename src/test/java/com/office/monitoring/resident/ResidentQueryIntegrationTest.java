package com.office.monitoring.resident;

import com.office.monitoring.member.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 거주자 기능의 조건별 응답과 저장 결과를 검증하는 테스트 클래스. */
class ResidentQueryIntegrationTest extends ResidentIntegrationTestSupport {

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void 본인에게_연결된_거주자조회_성공() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("박순자")
                .birthDate(LocalDate.of(1940, 2, 14))
                .address("서울시 종로구 어딘가")
                .phone("010-1111-2222")
                .disease("당뇨")
                .latitude(37.57)
                .longitude(126.98)
                .build());

        Member member = memberRepository.findByUsername("user").orElseThrow();
        member.assignResident(resident.getId());
        memberRepository.save(member);

        mockMvc.perform(get("/api/v1/residents/{id}", resident.getId())
                        .with(user("user").roles("FAMILY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.resident.name").value("박순자"));
    }

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void ADMIN은_임의의_거주자조회_성공() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("관리대상자")
                .birthDate(LocalDate.of(1938, 1, 1))
                .address("서울시 중구 어딘가")
                .phone("010-9999-0000")
                .disease("관절염")
                .latitude(37.55)
                .longitude(126.99)
                .build());

        mockMvc.perform(get("/api/v1/residents/{id}", resident.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.resident.name").value("관리대상자"));
    }

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void 연결되지않은_거주자조회시_403() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("남의 거주자")
                .birthDate(LocalDate.of(1944, 9, 9))
                .address("서울시 어딘가")
                .phone("010-2222-3333")
                .disease("없음")
                .latitude(37.50)
                .longitude(127.00)
                .build());

        mockMvc.perform(get("/api/v1/residents/{id}", resident.getId())
                        .with(user("user").roles("FAMILY")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("해당 거주자 정보에 접근할 수 없습니다."));
    }

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void 존재하지않는_거주자조회시_400() throws Exception {
        mockMvc.perform(get("/api/v1/residents/999999")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("거주자 정보를 찾을 수 없습니다."));
    }
}
