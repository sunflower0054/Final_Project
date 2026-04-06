package com.office.monitoring.resident;

import com.office.monitoring.aiSettings.AiSettingsRepository;
import com.office.monitoring.event.EventRepository;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.member.Member;
import com.office.monitoring.member.Role;
import com.office.monitoring.resident.dto.ResidentCreateRequest;
import com.office.monitoring.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/** ResidentServiceTest 테스트를 정의한다. */
class ResidentServiceTest {

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private AiSettingsRepository aiSettingsRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ResidentHistoryRepository residentHistoryRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ResidentService residentService;

/** 신규등록시_기존_ai설정이_있으면_기본_ai설정_insert를_생략한다 시나리오를 검증한다. */
    @Test
    void 신규등록시_기존_ai설정이_있으면_기본_ai설정_insert를_생략한다() {
        Member currentMember = Member.builder()
                .id(1L)
                .username("user")
                .password("encoded")
                .name("사용자")
                .phone("010-0000-0000")
                .role(Role.FAMILY)
                .build();

        Resident savedResident = Resident.builder()
                .id(10L)
                .name("거주자")
                .birthDate(LocalDate.of(1940, 1, 1))
                .address("서울")
                .latitude(37.0)
                .longitude(127.0)
                .build();

        ResidentCreateRequest request = new ResidentCreateRequest(
                "  거주자  ",
                LocalDate.of(1940, 1, 1),
                "  서울  ",
                " ",
                "  고혈압  ",
                37.0,
                127.0
        );

        when(currentUserService.getCurrentMember()).thenReturn(currentMember);
        when(residentRepository.save(any(Resident.class))).thenReturn(savedResident);
        when(aiSettingsRepository.existsByResidentId(10L)).thenReturn(true);

        Long residentId = residentService.createResident(request);

        assertThat(residentId).isEqualTo(10L);
        assertThat(currentMember.getResidentId()).isEqualTo(10L);
        verify(aiSettingsRepository, never()).save(any());
    }

/** 신규등록시_ai설정이_없으면_기존_기본값으로_생성한다 시나리오를 검증한다. */
    @Test
    void 신규등록시_ai설정이_없으면_기존_기본값으로_생성한다() {
        Member currentMember = Member.builder()
                .id(1L)
                .username("user")
                .password("encoded")
                .name("사용자")
                .phone("010-0000-0000")
                .role(Role.FAMILY)
                .build();

        Resident savedResident = Resident.builder()
                .id(11L)
                .name("거주자")
                .birthDate(LocalDate.of(1941, 1, 1))
                .address("서울")
                .latitude(37.0)
                .longitude(127.0)
                .build();

        ResidentCreateRequest request = new ResidentCreateRequest(
                "거주자",
                LocalDate.of(1941, 1, 1),
                "서울",
                null,
                null,
                37.0,
                127.0
        );

        when(currentUserService.getCurrentMember()).thenReturn(currentMember);
        when(residentRepository.save(any(Resident.class))).thenReturn(savedResident);
        when(aiSettingsRepository.existsByResidentId(11L)).thenReturn(false);

        residentService.createResident(request);

        ArgumentCaptor<com.office.monitoring.aiSettings.AiSettings> captor = ArgumentCaptor.forClass(com.office.monitoring.aiSettings.AiSettings.class);
        verify(aiSettingsRepository).save(captor.capture());

        assertThat(captor.getValue().getResidentId()).isEqualTo(11L);
        assertThat(captor.getValue().getFallSensitivity()).isEqualTo(0.1D);
        assertThat(captor.getValue().getNoMotionThreshold()).isEqualTo(1800);
        assertThat(captor.getValue().getVelocityThreshold()).isEqualTo(0.15D);
    }
}
