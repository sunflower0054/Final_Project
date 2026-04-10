package com.office.monitoring.sms;

import com.office.monitoring.event.Event;
import com.office.monitoring.event.EventRepository;
import com.office.monitoring.member.Member;
import com.office.monitoring.resident.Resident;
import com.office.monitoring.resident.ResidentRepository;
import com.office.monitoring.security.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;

@Controller
@RequestMapping("/report")
@RequiredArgsConstructor
public class SmsController {

    private final EventRepository eventRepository;
    private final SmsService smsService;
    private final ResidentRepository residentRepository;
    private final CurrentUserService currentUserService;


    // 119 신고 버튼 클릭
    @GetMapping("/confirm")
    public String confirm(@RequestParam Long eventId, Model model) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 없음"));

        Resident resident = residentRepository.findById(event.getResidentId())
                .orElseThrow(() -> new IllegalArgumentException("거주자 없음"));

        Member loginMember = currentUserService.getCurrentMember();
        String reporterPhone = loginMember.getPhone();

        event.setStatus("CONFIRMED");
        eventRepository.save(event);

        boolean sent = smsService.sendManualReport(event, reporterPhone);

        if (sent) {
            int age = Period.between(resident.getBirthDate(), LocalDate.now()).getYears();
            String messageContent = String.format(
                    "[긴급신고] %s 감지\n"
                            + "이름: %s / %d세\n"
                            + "기저질환: %s\n"
                            + "주소: %s\n"
                            + "거주자 연락처: %s\n"
                            + "신고자 연락처: %s",
                    event.getEventType(),
                    resident.getName(), age,
                    resident.getDisease() != null ? resident.getDisease() : "없음",
                    resident.getAddress(),
                    resident.getPhone() != null ? resident.getPhone() : "미등록",
                    reporterPhone
            );

            model.addAttribute("messageContent", messageContent);
            model.addAttribute("resident", resident);
            model.addAttribute("age", age);
            model.addAttribute("reporterPhone", reporterPhone);

            // 여기 수정!! (manuReport → manualReport)
            return "report/manuReport";     // ←←← 이 부분을 manualReport로 변경

        } else {
            return "report/failReport";       // failReport도 실제 파일명과 맞춰주세요
        }
    }

    // 상황 종료 버튼 클릭 → 페이지 이동 없이 DB만 변경
    @ResponseBody
    @GetMapping("/close")
    public ResponseEntity<String> close(@RequestParam Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 없음"));

        event.setStatus("CLOSED");
        eventRepository.save(event);

        return ResponseEntity.ok("상황이 종료되었습니다.");
    }

    // 가장 최근 AUTO_REPORTED 이벤트 자동 조회
    @GetMapping("/auto")
    public String autoReport(HttpSession session, Model model) {

        // 1) 가장 최근 AUTO_REPORTED 이벤트 자동 조회
        Event event = eventRepository.findTopByStatusOrderByCreatedAtDesc("AUTO_REPORTED")
                .orElseThrow(() -> new IllegalArgumentException("자동신고 이벤트 없음"));

        // 2) 거주자 조회
        Resident resident = residentRepository.findById(event.getResidentId())
                .orElseThrow(() -> new IllegalArgumentException("거주자 없음"));

        // 3) 신고자 연락처 (세션에서 꺼내기)
        Member loginMember = (Member) session.getAttribute("loginMember");
        String reporterPhone = loginMember.getPhone();

        // 4) 발송된 문자 내용 조립
        int age = Period.between(resident.getBirthDate(), LocalDate.now()).getYears();
        String messageContent = String.format(
                "[긴급신고] %s 감지\n"
                        + "이름: %s / %d세\n"
                        + "기저질환: %s\n"
                        + "주소: %s\n"
                        + "거주자 연락처: %s\n"
                        + "신고자 연락처: %s",
                event.getEventType(),
                resident.getName(), age,
                resident.getDisease() != null ? resident.getDisease() : "없음",
                resident.getAddress(),
                resident.getPhone() != null ? resident.getPhone() : "미등록",
                reporterPhone
        );

        model.addAttribute("messageContent", messageContent);
        model.addAttribute("resident", resident);
        model.addAttribute("age", age);
        model.addAttribute("reporterPhone", reporterPhone);
        return "autoReport";    // templates/autoReport.html
    }
}






