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
/** ResidentService의 역할을 담당한다. */
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final AiSettingsRepository aiSettingsRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final ResidentHistoryRepository residentHistoryRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    /** createResident 동작을 수행한다. */
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
    /** updateResident 동작을 수행한다. */
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
    /** deleteResident 동작을 수행한다. */
    public void deleteResident(Long residentId) {
        Resident resident = getAuthorizedResident(residentId);

        if (hasHistoryData(residentId)) {
            throw new ResidentDeletionBlockedException("이력 데이터가 있어 삭제할 수 없습니다.");
        }

        memberRepository.clearResidentReference(residentId);
        aiSettingsRepository.deleteByResidentId(residentId);
        residentRepository.delete(resident);
    }

    /** getResident 동작을 수행한다. */
    public ResidentResponse getResident(Long residentId) {
        Resident resident = getAuthorizedResident(residentId);
        return ResidentResponse.from(resident);
    }

    /** getResidents 동작을 수행한다. */
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

    /** getAuthorizedResident 동작을 수행한다. */
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

    /** trimToNull 동작을 수행한다. */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** createDefaultAiSettingsIfAbsent 동작을 수행한다. */
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

    /** hasHistoryData 동작을 수행한다. */
    private boolean hasHistoryData(Long residentId) {
        return eventRepository.existsByResidentId(residentId)
                || residentHistoryRepository.existsDailyActivityByResidentId(residentId);
    }
}
