package com.office.monitoring.admin;

import com.office.monitoring.event.EventRepository;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.resident.ResidentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private AdminStatsService adminStatsService;

    @Test
    void getUserStats_회원연령대를_실데이터로_집계한다() {
        int currentYear = LocalDate.now().getYear();
        when(memberRepository.findAllForAdminStats()).thenReturn(List.of(
                memberStats(currentYear - 18, "학생"),
                memberStats(currentYear - 25, "보호자"),
                memberStats(currentYear - 35, "초기 목적"),
                memberStats(currentYear - 45, "방범"),
                memberStats(currentYear - 55, "관제"),
                memberStats(currentYear - 65, "관제"),
                memberStats(null, null)
        ));

        AdminStatsDTO.UserStatsResponse response = adminStatsService.getUserStats();

        assertThat(response.totalUsers()).isEqualTo(7);
        assertThat(response.ageGroups()).containsExactly(
                List.of("연령대", "인원 수"),
                List.of("10대", 1L),
                List.of("20대", 1L),
                List.of("30대", 1L),
                List.of("40대", 1L),
                List.of("50대", 1L),
                List.of("60대 이상", 1L),
                List.of("미상", 1L)
        );
    }

    @Test
    void getUserStats_가입목적을_trim하고_null_blank는_미지정으로_집계한다() {
        when(memberRepository.findAllForAdminStats()).thenReturn(List.of(
                memberStats(1990, " 보호자 "),
                memberStats(1988, "보호자"),
                memberStats(1985, "방범 및 모니터링"),
                memberStats(1982, "초기 목적"),
                memberStats(1979, ""),
                memberStats(null, null)
        ));

        AdminStatsDTO.UserStatsResponse response = adminStatsService.getUserStats();

        assertThat(response.totalUsers()).isEqualTo(6);
        assertThat(response.purposes()).containsExactly(
                List.of("이용 목적", "회원 수"),
                List.of("방범 및 모니터링", 1L),
                List.of("보호자", 2L),
                List.of("초기 목적", 1L),
                List.of("미지정", 2L)
        );
    }

    @Test
    void getResidentStats_현재날짜기준_평균나이를_소수점첫째자리로_반환한다() {
        LocalDate today = LocalDate.now();
        when(residentRepository.findAllForAdminStats()).thenReturn(List.of(
                residentStats(today.minusYears(64)),
                residentStats(today.minusYears(74)),
                residentStats(today.minusYears(84)),
                residentStats(today.minusYears(54))
        ));

        AdminStatsDTO.ResidentStatsResponse response = adminStatsService.getResidentStats();

        assertThat(response.totalResidents()).isEqualTo(4);
        assertThat(response.averageAge()).isEqualTo(69.0);
    }

    @Test
    void getResidentStats_거주자연령대를_60대_70대_80대이상_기타로_집계한다() {
        LocalDate today = LocalDate.now();
        when(residentRepository.findAllForAdminStats()).thenReturn(List.of(
                residentStats(today.minusYears(64)),
                residentStats(today.minusYears(74)),
                residentStats(today.minusYears(84)),
                residentStats(today.minusYears(54))
        ));

        AdminStatsDTO.ResidentStatsResponse response = adminStatsService.getResidentStats();

        assertThat(response.ageGroups()).containsExactly(
                List.of("연령대", "인원 수"),
                List.of("60대", 1L),
                List.of("70대", 1L),
                List.of("80대 이상", 1L),
                List.of("기타", 1L)
        );
    }

    @Test
    void getEventStats_타입별상태별집계를_실데이터로_구성하고_PENDING은_byStatus에서_제외한다() {
        when(eventRepository.findAllForAdminStats()).thenReturn(List.of(
                eventStats("FALL_DETECTED", "CONFIRMED", LocalDateTime.of(2026, 1, 5, 10, 0)),
                eventStats("NO_MOTION_DETECTED", "AUTO_REPORTED", LocalDateTime.of(2026, 1, 6, 10, 0)),
                eventStats("VIOLENT_MOTION_DETECTED", "CLOSED", LocalDateTime.of(2026, 2, 1, 9, 0)),
                eventStats("FALL_DETECTED", "PENDING", LocalDateTime.of(2026, 2, 2, 9, 0)),
                eventStats("UNKNOWN_TYPE", "CONFIRMED", LocalDateTime.of(2026, 2, 3, 9, 0))
        ));

        AdminStatsDTO.EventStatsResponse response = adminStatsService.getEventStats(null, null);

        assertThat(response.totalEvents()).isEqualTo(5);
        assertThat(response.byType()).containsExactly(
                List.of("유형", "건수"),
                List.of("낙상", 2L),
                List.of("움직임 없음", 1L),
                List.of("폭행", 1L)
        );
        assertThat(response.byStatus()).containsExactly(
                List.of("상태", "건수"),
                List.of("수동신고", 2L),
                List.of("자동신고", 1L),
                List.of("오탐지", 1L)
        );
        assertThat(response.monthlyTrend()).containsExactly(
                List.of("월", "낙상", "움직임 없음", "폭행"),
                List.of("2026-01", 1L, 1L, 0L),
                List.of("2026-02", 1L, 0L, 1L)
        );
    }

    @Test
    void getEventStats_year만_있으면_해당연도_12개월기준으로_집계한다() {
        when(eventRepository.findAllForAdminStatsBetween(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2027, 1, 1, 0, 0)
        )).thenReturn(List.of(
                eventStats("FALL_DETECTED", "CONFIRMED", LocalDateTime.of(2026, 1, 10, 8, 0)),
                eventStats("NO_MOTION_DETECTED", "AUTO_REPORTED", LocalDateTime.of(2026, 3, 10, 8, 0)),
                eventStats("VIOLENT_MOTION_DETECTED", "CLOSED", LocalDateTime.of(2026, 12, 10, 8, 0))
        ));

        AdminStatsDTO.EventStatsResponse response = adminStatsService.getEventStats(2026, null);

        assertThat(response.totalEvents()).isEqualTo(3);
        assertThat(response.monthlyTrend()).containsExactly(
                List.of("월", "낙상", "움직임 없음", "폭행"),
                List.of("1월", 1L, 0L, 0L),
                List.of("2월", 0L, 0L, 0L),
                List.of("3월", 0L, 1L, 0L),
                List.of("4월", 0L, 0L, 0L),
                List.of("5월", 0L, 0L, 0L),
                List.of("6월", 0L, 0L, 0L),
                List.of("7월", 0L, 0L, 0L),
                List.of("8월", 0L, 0L, 0L),
                List.of("9월", 0L, 0L, 0L),
                List.of("10월", 0L, 0L, 0L),
                List.of("11월", 0L, 0L, 0L),
                List.of("12월", 0L, 0L, 1L)
        );
    }

    @Test
    void getEventStats_year와_month가_있으면_해당월만_집계한다() {
        when(eventRepository.findAllForAdminStatsBetween(
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 5, 1, 0, 0)
        )).thenReturn(List.of(
                eventStats("FALL_DETECTED", "CONFIRMED", LocalDateTime.of(2026, 4, 8, 9, 0)),
                eventStats("NO_MOTION_DETECTED", "PENDING", LocalDateTime.of(2026, 4, 9, 9, 0))
        ));

        AdminStatsDTO.EventStatsResponse response = adminStatsService.getEventStats(2026, 4);

        assertThat(response.totalEvents()).isEqualTo(2);
        assertThat(response.byStatus()).containsExactly(
                List.of("상태", "건수"),
                List.of("수동신고", 1L),
                List.of("자동신고", 0L),
                List.of("오탐지", 0L)
        );
        assertThat(response.monthlyTrend()).containsExactly(
                List.of("월", "낙상", "움직임 없음", "폭행"),
                List.of("4월", 1L, 1L, 0L)
        );
    }

    @Test
    void getEventStats_month만_있으면_안전하게_전체기준으로_처리한다() {
        when(eventRepository.findAllForAdminStats()).thenReturn(List.of(
                eventStats("FALL_DETECTED", "CONFIRMED", LocalDateTime.of(2025, 12, 1, 8, 0)),
                eventStats("VIOLENT_MOTION_DETECTED", "AUTO_REPORTED", LocalDateTime.of(2026, 1, 1, 8, 0))
        ));

        AdminStatsDTO.EventStatsResponse response = adminStatsService.getEventStats(null, 4);

        assertThat(response.totalEvents()).isEqualTo(2);
        assertThat(response.monthlyTrend()).containsExactly(
                List.of("월", "낙상", "움직임 없음", "폭행"),
                List.of("2025-12", 1L, 0L, 0L),
                List.of("2026-01", 0L, 0L, 1L)
        );
    }

    private MemberRepository.AdminStatsView memberStats(Integer birthYear, String purpose) {
        return new MemberRepository.AdminStatsView() {
            @Override
            public Integer getBirthYear() {
                return birthYear;
            }

            @Override
            public String getPurpose() {
                return purpose;
            }
        };
    }

    private ResidentRepository.AdminStatsView residentStats(LocalDate birthDate) {
        return new ResidentRepository.AdminStatsView() {
            @Override
            public LocalDate getBirthDate() {
                return birthDate;
            }
        };
    }

    private EventRepository.AdminStatsView eventStats(String eventType, String status, LocalDateTime timestamp) {
        return new EventRepository.AdminStatsView() {
            @Override
            public String getEventType() {
                return eventType;
            }

            @Override
            public String getStatus() {
                return status;
            }

            @Override
            public LocalDateTime getTimestamp() {
                return timestamp;
            }
        };
    }
}
