package com.office.monitoring.index;

import com.office.monitoring.event.Event;
import com.office.monitoring.event.EventRepository;
import com.office.monitoring.resident.Resident;
import com.office.monitoring.resident.ResidentRepository;
import com.office.monitoring.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class IndexController {

    private final ResidentRepository residentRepository;
    private final CurrentUserService currentUserService;
    private final EventRepository eventRepository;

    @GetMapping({"", "/", "/index", "/index/index"})
    public String index(@RequestParam(name = "residentId", required = false) Long residentId, Model model) {

        Resident resident = null;

        // 1. URL에 residentId 직접 전달된 경우 (관리자 테스트용)
        if (residentId != null) {
            resident = residentRepository.findById(residentId).orElse(null);
        }
        // 2. 로그인한 사용자 기준으로 residentId 가져오기
        else {
            try {
                Long loggedInResidentId = currentUserService.getResidentId();
                resident = residentRepository.findById(loggedInResidentId).orElse(null);
            } catch (Exception e) {
                log.warn("로그인 사용자 residentId 조회 실패: {}", e.getMessage());
                model.addAttribute("resident", null);
                model.addAttribute("errorMessage", "⚠️ 거주자 정보가 등록되지 않았습니다.");
                return "index/index";
            }
        }

        if (resident == null) {
            model.addAttribute("errorMessage", "⚠️ 거주자 정보를 찾을 수 없습니다.");
            return "index/index";
        }

        // ★★★ 핵심 수정 ★★★
        // PENDING + CONFIRMED 모두 조회 (실시간 알림이 CONFIRMED 상태에서도 보이게)
        Optional<Event> activeEvent = eventRepository.findTopByResidentIdAndStatusInOrderByCreatedAtDesc(
                resident.getId(), List.of("PENDING", "CONFIRMED"));

        if (activeEvent.isPresent()) {
            Event event = activeEvent.get();
            model.addAttribute("event", event);      // urgent-alert용
            model.addAttribute("eventId", event.getId()); // 버튼용
            log.info("[Index] 실시간 이벤트 발견 - eventId={}, status={}", event.getId(), event.getStatus());
        }

        model.addAttribute("resident", resident);
        return "index/index";
    }
}