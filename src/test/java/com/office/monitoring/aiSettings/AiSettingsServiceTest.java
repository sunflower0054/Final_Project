package com.office.monitoring.aiSettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/** AiSettingsServiceTest 테스트를 정의한다. */
class AiSettingsServiceTest {

    @Mock
    private AiSettingsRepository aiSettingsRepository;

    @InjectMocks
    private AiSettingsService aiSettingsService;

    @Test
/** save_기존_설정이_있으면_값을_갱신하고_updatedAt을_응답에_반영한다 시나리오를 검증한다. */
    void save_기존_설정이_있으면_값을_갱신하고_updatedAt을_응답에_반영한다() {
        AiSettings existing = AiSettings.builder()
                .id(1L)
                .residentId(1L)
                .fallSensitivity(0.1D)
                .noMotionThreshold(1800)
                .velocityThreshold(0.15D)
                .build();

        AiSettingsDto dto = new AiSettingsDto();
        dto.setFallSensitivity(0.25D);
        dto.setNoMotionThreshold(2400);
        dto.setVelocityThreshold(0.33D);

        when(aiSettingsRepository.findByResidentId(1L)).thenReturn(Optional.of(existing));

        AiSettingsDto saved = aiSettingsService.save(1L, dto);

        verify(aiSettingsRepository).save(existing);
        assertThat(existing.getFallSensitivity()).isEqualTo(0.25D);
        assertThat(existing.getNoMotionThreshold()).isEqualTo(2400);
        assertThat(existing.getVelocityThreshold()).isEqualTo(0.33D);
        assertThat(existing.getUpdatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
/** save_기존_설정이_없으면_새_설정을_생성한다 시나리오를 검증한다. */
    void save_기존_설정이_없으면_새_설정을_생성한다() {
        AiSettingsDto dto = new AiSettingsDto();
        dto.setFallSensitivity(0.4D);
        dto.setNoMotionThreshold(3600);
        dto.setVelocityThreshold(0.5D);

        when(aiSettingsRepository.findByResidentId(7L)).thenReturn(Optional.empty());

        aiSettingsService.save(7L, dto);

        ArgumentCaptor<AiSettings> captor = ArgumentCaptor.forClass(AiSettings.class);
        verify(aiSettingsRepository).save(captor.capture());

        AiSettings saved = captor.getValue();
        assertThat(saved.getResidentId()).isEqualTo(7L);
        assertThat(saved.getFallSensitivity()).isEqualTo(0.4D);
        assertThat(saved.getNoMotionThreshold()).isEqualTo(3600);
        assertThat(saved.getVelocityThreshold()).isEqualTo(0.5D);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
/** get_저장된_설정을_조회하면_DTO로_매핑한다 시나리오를 검증한다. */
    void get_저장된_설정을_조회하면_DTO로_매핑한다() {
        AiSettings settings = AiSettings.builder()
                .residentId(3L)
                .fallSensitivity(0.2D)
                .noMotionThreshold(2100)
                .velocityThreshold(0.17D)
                .updatedAt(LocalDateTime.of(2026, 4, 6, 10, 20, 30))
                .build();

        when(aiSettingsRepository.findByResidentId(3L)).thenReturn(Optional.of(settings));

        AiSettingsDto dto = aiSettingsService.get(3L);

        assertThat(dto.getFallSensitivity()).isEqualTo(0.2D);
        assertThat(dto.getNoMotionThreshold()).isEqualTo(2100);
        assertThat(dto.getVelocityThreshold()).isEqualTo(0.17D);
        assertThat(dto.getUpdatedAt()).isEqualTo("2026-04-06 10:20:30");
    }

    @Test
/** get_updatedAt이_null이면_대시를_반환한다 시나리오를 검증한다. */
    void get_updatedAt이_null이면_대시를_반환한다() {
        AiSettings settings = AiSettings.builder()
                .residentId(4L)
                .fallSensitivity(0.2D)
                .noMotionThreshold(2100)
                .velocityThreshold(0.17D)
                .updatedAt(null)
                .build();

        when(aiSettingsRepository.findByResidentId(4L)).thenReturn(Optional.of(settings));

        AiSettingsDto dto = aiSettingsService.get(4L);

        assertThat(dto.getUpdatedAt()).isEqualTo("-");
    }

    @Test
/** get_설정이_없으면_예외를_던진다 시나리오를 검증한다. */
    void get_설정이_없으면_예외를_던진다() {
        when(aiSettingsRepository.findByResidentId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiSettingsService.get(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("AI 설정이 존재하지 않습니다.");
    }
}
