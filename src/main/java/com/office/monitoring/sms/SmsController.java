package com.office.monitoring.sms;

import com.office.monitoring.event.Event;
import com.office.monitoring.event.EventRepository;
import com.office.monitoring.member.Member;
import com.office.monitoring.resident.Resident;
import com.office.monitoring.resident.ResidentRepository;
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

    // 119 신고 버튼 클릭
    @GetMapping("/confirm")
    public String confirm(@RequestParam Long eventId,
                          HttpSession session,
                          Model model) {

        // 1) 이벤트 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 없음"));

        // 2) 거주자 조회 (이벤트 → residentId → 거주자 한 줄)
        Resident resident = residentRepository.findById(event.getResidentId())
                .orElseThrow(() -> new IllegalArgumentException("거주자 없음"));

        // 3) 신고자 연락처 (세션에서 꺼내기)
        Member loginMember = (Member) session.getAttribute("loginMember");
        String reporterPhone = loginMember.getPhone();

        // 스케줄러 만료 처리
        event.setStatus("CONFIRMED");
        eventRepository.save(event);

        // 119 수동 문자 전송 (신고자 연락처는 추후 로그인 유저에서 가져오기)
        boolean sent = smsService.sendManualReport(event, "01083763942");


        if (sent) {
            // 6) 발송된 문자 내용 조립 (화면에 보여줄 내용)
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
            return "manuReport";    // templates/manuReport.html

        } else {
            return "failReport";    // templates/failReport.html
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

    // 자동신고 완료 페이지 (주소창에 직접 입력)
// 예시: /report/auto/63
    @GetMapping("/auto/{eventId}")
    public String autoReport(@PathVariable Long eventId,
                             HttpSession session,
                             Model model) {

        // 1) 이벤트 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 없음"));

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
                        + "보호자 연락처: %s",
                event.getEventType(),
                resident.getName(), age,
                resident.getDisease() != null ? resident.getDisease() : "없음",
                resident.getAddress(),
                resident.getPhone() != null ? resident.getPhone() : "미등록",
                reporterPhone
        );

        model.addAttribute("messageContent", messageContent);
        model.addAttribute("resident", resident);
        return "autoReport";    // templates/autoReport.html
    }
}






