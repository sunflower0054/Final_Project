package com.office.monitoring.resident;

import com.office.monitoring.aiSettings.AiSettings;
import com.office.monitoring.event.Event;
import com.office.monitoring.member.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** ResidentDeleteIntegrationTest 테스트를 정의한다. */
class ResidentDeleteIntegrationTest extends ResidentIntegrationTestSupport {

/** 이력이_없는_거주자는_삭제되고_회원연결과_ai설정이_정리된다 시나리오를 검증한다. */
    @Test
    void 이력이_없는_거주자는_삭제되고_회원연결과_ai설정이_정리된다() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("삭제 대상")
                .birthDate(LocalDate.of(1940, 1, 1))
                .address("서울시 삭제구")
                .phone("010-1111-2222")
                .disease("고혈압")
                .latitude(37.55)
                .longitude(126.99)
                .build());

        aiSettingsRepository.save(AiSettings.builder()
                .residentId(resident.getId())
                .fallSensitivity(0.1D)
                .noMotionThreshold(1800)
                .velocityThreshold(0.15D)
                .build());

        Member userMember = memberRepository.findByUsername("user").orElseThrow();
        userMember.assignResident(resident.getId());
        memberRepository.save(userMember);

        Member secondMember = memberRepository.findByUsername("new-user").orElseThrow();
        secondMember.assignResident(resident.getId());
        memberRepository.save(secondMember);

        mockMvc.perform(delete("/api/v1/residents/{id}", resident.getId())
                        .with(user("user").roles("FAMILY"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("거주자 정보가 삭제되었습니다."));

        assertThat(residentRepository.findById(resident.getId())).isEmpty();
        assertThat(aiSettingsRepository.findByResidentId(resident.getId())).isEmpty();
        assertThat(memberRepository.findByUsername("user").orElseThrow().getResidentId()).isNull();
        assertThat(memberRepository.findByUsername("new-user").orElseThrow().getResidentId()).isNull();
    }

/** 이벤트_이력이_있으면_409로_삭제가_차단된다 시나리오를 검증한다. */
    @Test
    void 이벤트_이력이_있으면_409로_삭제가_차단된다() throws Exception {
        Resident resident = residentRepository.save(Resident.builder()
                .name("이력 보존 대상")
                .birthDate(LocalDate.of(1942, 2, 2))
                .address("서울시 이력구")
                .phone("010-3333-4444")
                .disease("당뇨")
                .latitude(37.56)
                .longitude(127.01)
                .build());

        Member member = memberRepository.findByUsername("user").orElseThrow();
        member.assignResident(resident.getId());
        memberRepository.save(member);

        eventRepository.save(Event.builder()
                .residentId(resident.getId())
                .eventType("FALL_DETECTED")
                .timestamp(LocalDateTime.now())
                .confidence(0.95D)
                .status("CONFIRMED")
                .build());

        mockMvc.perform(delete("/api/v1/residents/{id}", resident.getId())
                        .with(user("user").roles("FAMILY"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이력 데이터가 있어 삭제할 수 없습니다."));

        assertThat(residentRepository.findById(resident.getId())).isPresent();
        assertThat(memberRepository.findByUsername("user").orElseThrow().getResidentId()).isEqualTo(resident.getId());
    }

/** daily_activity_이력이_있으면_409로_삭제가_차단된다 시나리오를 검증한다. */
    @Test
    void daily_activity_이력이_있으면_409로_삭제가_차단된다() throws Exception {
        createDailyActivityTableIfNeeded();

        Resident resident = residentRepository.save(Resident.builder()
                .name("활동 이력 대상")
                .birthDate(LocalDate.of(1945, 3, 3))
                .address("서울시 활동구")
                .phone("010-5555-6666")
                .disease("관절염")
                .latitude(37.58)
                .longitude(127.02)
                .build());

        Member member = memberRepository.findByUsername("user").orElseThrow();
        member.assignResident(resident.getId());
        memberRepository.save(member);

        jdbcTemplate.update(
                "insert into daily_activity (resident_id, date, motion_score) values (?, current_date, ?)",
                resident.getId(),
                1234
        );

        mockMvc.perform(delete("/api/v1/residents/{id}", resident.getId())
                        .with(user("user").roles("FAMILY"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이력 데이터가 있어 삭제할 수 없습니다."));

        assertThat(residentRepository.findById(resident.getId())).isPresent();
    }
}
