package com.office.monitoring.resident;

import com.office.monitoring.aiSettings.AiSettings;
import com.office.monitoring.aiSettings.AiSettingsRepository;
import com.office.monitoring.event.EventRepository;
import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.member.Role;
import com.office.monitoring.resident.dto.ResidentCreateRequest;
import com.office.monitoring.resident.dto.ResidentResponse;
import com.office.monitoring.resident.dto.ResidentUpdateRequest;
import com.office.monitoring.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/** 거주자 데이터 조회·등록·수정·삭제 흐름을 담당하는 구성 요소. */
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final AiSettingsRepository aiSettingsRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final ResidentHistoryRepository residentHistoryRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    /** 요청 데이터를 거주자 기준으로 저장하고 저장 결과를 반환한다. */
    public Long createResident(ResidentCreateRequest request) {
        Member currentMember = currentUserService.getCurrentMember();

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
        memberRepository.saveAndFlush(currentMember);

        createDefaultAiSettingsIfAbsent(savedResident.getId());

        return savedResident.getId();
    }

    @Transactional
    /** 수정 요청값을 기존 거주자 정보에 반영하고 최신 결과를 반환한다. */
    public void updateResident(Long residentId, ResidentUpdateRequest request) {
        Resident resident = getAuthorizedResident(residentId);
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

    @Transactional
    /** 대상 거주자 정보를 삭제 또는 탈퇴 처리하고 완료 결과를 반환한다. */
    public void deleteResident(Long residentId) {
        Resident resident = getAuthorizedResident(residentId);

        if (hasHistoryData(residentId)) {
            throw new ResidentDeletionBlockedException("이력 데이터가 있어 삭제할 수 없습니다.");
        }

        memberRepository.clearResidentReference(residentId);
        aiSettingsRepository.deleteByResidentId(residentId);
        residentRepository.delete(resident);
    }

    /** 거주자 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
    public ResidentResponse getResident(Long residentId) {
        Resident resident = getAuthorizedResident(residentId);
        return ResidentResponse.from(resident);
    }

    /** 거주자 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
    public List<ResidentResponse> getResidents() {
        Member currentMember = currentUserService.getCurrentMember();

        if (currentMember.getRole() == Role.ADMIN) {
            return residentRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                    .map(ResidentResponse::from)
                    .toList();
        }

        if (currentMember.getResidentId() == null) {
            return List.of();
        }

        return residentRepository.findById(currentMember.getResidentId())
                .map(ResidentResponse::from)
                .map(List::of)
                .orElseGet(List::of);
    }

    /** 거주자 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
    private Resident getAuthorizedResident(Long residentId) {
        Member currentMember = currentUserService.getCurrentMember();

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

    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** 요청 데이터를 거주자 기준으로 저장하고 저장 결과를 반환한다. */
    private void createDefaultAiSettingsIfAbsent(Long residentId) {
        // orphan 데이터 등으로 resident_id 기준 설정이 이미 있으면 중복 INSERT를 방지한다.
        if (aiSettingsRepository.existsByResidentId(residentId)) {
            return;
        }

        aiSettingsRepository.save(AiSettings.builder()
                .residentId(residentId)
                .fallSensitivity(0.1D)
                .noMotionThreshold(1800)
                .velocityThreshold(0.15D)
                .build());
    }

    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    private boolean hasHistoryData(Long residentId) {
        return eventRepository.existsByResidentId(residentId)
                || residentHistoryRepository.existsDailyActivityByResidentId(residentId);
    }
}
