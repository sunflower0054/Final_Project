package com.office.monitoring.aiSettings;

import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSettingsService {

    private final AiSettingsRepository aiSettingsRepository;
    private final MemberRepository memberRepository;

    private static final String PYTHON_URL = "http://localhost:5005";

    // 현재 로그인한 사용자의 설정값 조회
    public AiSettings getSettings(String username) {
        Member member = getMemberByUsername(username);
        Long residentId = getResidentIdOrThrow(member);

        AiSettings settings = aiSettingsRepository.findByResidentId(residentId);
        if (settings == null) {
            throw new IllegalStateException("해당 거주자의 AI 설정이 존재하지 않습니다.");
        }

        return settings;
    }

    // 현재 로그인한 사용자의 설정값 저장 + 파이썬으로 즉시 전달
    public AiSettings updateSettings(String username, AiSettingsDto dto) {
        Member member = getMemberByUsername(username);
        Long residentId = getResidentIdOrThrow(member);

        AiSettings settings = aiSettingsRepository.findByResidentId(residentId);

        if (settings == null) {
            settings = AiSettings.builder()
                    .residentId(residentId)
                    .fallSensitivity(0.1D)
                    .noMotionThreshold(1800)
                    .velocityThreshold(0.15D)
                    .build();
        }

        settings.setFallSensitivity(dto.getFallSensitivity());
        settings.setNoMotionThreshold(dto.getNoMotionThreshold());
        settings.setVelocityThreshold(dto.getVelocityThreshold());

        AiSettings saved = aiSettingsRepository.save(settings);

        log.info("설정값 DB 저장 완료 | residentId={} | fall={} | motion={} | velocity={}",
                residentId,
                saved.getFallSensitivity(),
                saved.getNoMotionThreshold(),
                saved.getVelocityThreshold());

        try {
            WebClient client = WebClient.create(PYTHON_URL);
            client.post()
                    .uri("/api/settings")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(response ->
                            log.info("파이썬 설정값 전달 완료: {}", response)
                    );
        } catch (Exception e) {
            log.error("파이썬 설정값 전달 실패", e);
        }

        return saved;
    }

    private Member getMemberByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("로그인한 사용자만 이용할 수 있습니다.");
        }

        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("현재 로그인한 사용자를 찾을 수 없습니다."));
    }

    private Long getResidentIdOrThrow(Member member) {
        if (member.getResidentId() == null) {
            throw new IllegalStateException("먼저 거주자 정보를 등록해야 합니다.");
        }
        return member.getResidentId();
    }
}
