package com.office.monitoring.resident;

import com.office.monitoring.aiSettings.AiSettings;
import com.office.monitoring.aiSettings.AiSettingsRepository;
import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.member.Role;
import com.office.monitoring.resident.dto.ResidentCreateRequest;
import com.office.monitoring.resident.dto.ResidentResponse;
import com.office.monitoring.resident.dto.ResidentUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final MemberRepository memberRepository;
    private final AiSettingsRepository aiSettingsRepository;

    @Transactional
    public Long createResident(String username, ResidentCreateRequest request) {
        Member currentMember = getMemberByUsername(username);

        if (currentMember.getResidentId() != null) {
            throw new IllegalStateException("이미 연결된 거주자 정보가 있습니다.");
        }

        Resident resident = Resident.builder()
                .name(request.name().trim())
                .birthDate(request.birthDate())
                .address(request.address().trim())
                .phone(trimToNull(request.phone()))
                .disease(trimToNull(request.disease()))
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        Resident savedResident = residentRepository.save(resident);

        currentMember.assignResident(savedResident.getId());

        aiSettingsRepository.save(AiSettings.builder()
                .residentId(savedResident.getId())
                .fallSensitivity(0.1D)
                .noMotionThreshold(1800)
                .velocityThreshold(0.15D)
                .build());

        return savedResident.getId();
    }

    @Transactional
    public void updateResident(String username, Long residentId, ResidentUpdateRequest request) {
        Resident resident = getAuthorizedResident(username, residentId);
        resident.update(
                request.name(),
                request.birthDate(),
                request.address(),
                request.phone(),
                request.disease(),
                request.latitude(),
                request.longitude()
        );
    }

    public ResidentResponse getResident(String username, Long residentId) {
        Resident resident = getAuthorizedResident(username, residentId);
        return ResidentResponse.from(resident);
    }

    private Resident getAuthorizedResident(String username, Long residentId) {
        Member currentMember = getMemberByUsername(username);

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("거주자 정보를 찾을 수 없습니다."));

        if (currentMember.getRole() == Role.ADMIN) {
            return resident;
        }

        if (currentMember.getResidentId() == null || !currentMember.getResidentId().equals(residentId)) {
            throw new AccessDeniedException("해당 거주자 정보에 접근할 수 없습니다.");
        }

        return resident;
    }

    private Member getMemberByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("로그인한 사용자만 이용할 수 있습니다.");
        }

        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("현재 로그인한 사용자를 찾을 수 없습니다."));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
