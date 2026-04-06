package com.office.monitoring.dailyActivity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyActivityServiceTest {

    @Mock
    private DailyActivityRepository dailyActivityRepository;

    @InjectMocks
    private DailyActivityService dailyActivityService;

    @Test
    void 기존_일일활동이_있으면_motion_score만_업데이트한다() {
        LocalDate date = LocalDate.of(2026, 4, 6);
        DailyActivity existing = DailyActivity.builder()
                .id(1L)
                .residentId(10L)
                .date(date)
                .motionScore(1200)
                .build();

        when(dailyActivityRepository.findByResidentIdAndDate(10L, date)).thenReturn(Optional.of(existing));

        dailyActivityService.save(10L, date, 3456);

        assertThat(existing.getMotionScore()).isEqualTo(3456);
        verify(dailyActivityRepository, never()).save(org.mockito.ArgumentMatchers.any(DailyActivity.class));
    }

    @Test
    void 기존_일일활동이_없으면_새_레코드를_저장한다() {
        LocalDate date = LocalDate.of(2026, 4, 6);
        when(dailyActivityRepository.findByResidentIdAndDate(20L, date)).thenReturn(Optional.empty());

        dailyActivityService.save(20L, date, 777);

        ArgumentCaptor<DailyActivity> captor = ArgumentCaptor.forClass(DailyActivity.class);
        verify(dailyActivityRepository).save(captor.capture());

        DailyActivity saved = captor.getValue();
        assertThat(saved.getResidentId()).isEqualTo(20L);
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getMotionScore()).isEqualTo(777);
    }
}
