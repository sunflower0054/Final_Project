package com.office.monitoring.sms;

import com.office.monitoring.event.Event;
import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.resident.Resident;
import com.office.monitoring.resident.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final MemberRepository memberRepository;
    private final ResidentRepository residentRepository;
    private final SmsLogRepository smsLogRepository;
    private final SmsProperties smsProperties;
    private final DefaultMessageService messageService;

    // ── 1차 알림: 가족에게 이벤트 감지 알림 ──────────────────────────
    public void sendFirstAlert(Event event) {
        List<Member> families = memberRepository.findAllByResidentId(event.getResidentId());
        if (families.isEmpty()) {
            log.warn("[SMS] residentId={} 에 연결된 가족 없음", event.getResidentId());
            return;
        }

        String message = "[AIoT 관제] 이상 행동이 감지되었습니다.\n"
                + "대시보드에서 확인해 주세요.\n"
                + "http://localhost:8091";

        for (Member family : families) {
            sendWithRetry(event.getId(), family.getPhone(), message, "FIRST_ALERT", 3);
        }
    }

    // ── 119 수동 신고 문자 ─────────────────────────────────────────
    public boolean sendManualReport(Event event, String reporterPhone) {
        Resident resident = findResident(event.getResidentId());
        String message = build119Message(event, resident, reporterPhone);
        return sendOnce(message, "119에 신고 문자");
    }

    // ── 119 자동 신고 문자 ─────────────────────────────────────────
    public boolean sendAutoReport(Event event) {
        Resident resident = findResident(event.getResidentId());

        // 신고자 연락처: 담당 가족 중 첫 번째
        List<Member> families = memberRepository.findAllByResidentId(event.getResidentId());
        String reporterPhone = families.isEmpty() ? "미등록" : families.get(0).getPhone();

        String message = build119Message(event, resident, reporterPhone);
        return sendOnce(message, "119 자동신고");
    }

    // ── 2차 알림: 자동신고 완료 후 가족에게 ──────────────────────────
    public void sendAutoReportAlert(Event event) {
        List<Member> families = memberRepository.findAllByResidentId(event.getResidentId());

        String message = "[AIoT 관제] 119에 자동 신고되었습니다.\n"
                + "아래 링크에서 확인하세요.\n"
                + "http://localhost:8091/report/auto";

        for (Member family : families) {
            sendWithRetry(event.getId(), family.getPhone(), message, "AUTO_REPORT", 3);
        }
    }

    // ── SOS 긴급 알림: 119 자동신고도 실패했을 때 ────────────────────
    public void sendSosAlert(Event event) {
        List<Member> families = memberRepository.findAllByResidentId(event.getResidentId());

        String message = "[AIoT 긴급] 119 자동신고에 실패했습니다!\n"
                + "직접 신고해 주세요!\n"
                + "http://localhost:8091";

        for (Member family : families) {
            sendWithRetry(event.getId(), family.getPhone(), message, "SOS_REPORT", 3);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 내부 유틸
    // ═══════════════════════════════════════════════════════════════

    /** 재시도 포함 전송 (지수 백오프) — sms_log 저장 */
    private void sendWithRetry(Long eventId, String phone, String text,
                               String smsType, int maxRetry) {
        boolean success = false;
        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                sendMessage(phone, text);
                success = true;
                log.info("[SMS] {} 전송 성공 → {}", smsType, phone);
                break;
            } catch (Exception e) {
                long waitMs = (long) Math.pow(2, attempt) * 1000;
                log.warn("[SMS] {} 전송 실패 ({}회차), {}ms 후 재시도", smsType, attempt, waitMs);
                sleep(waitMs);
            }
        }
        saveSmsLog(eventId, phone, text, smsType, success);
    }

    /** 단건 전송 — sms_log 저장 없음 (119 문자는 log 저장 안 함) */
    private boolean sendOnce(String text, String label) {
        // 119 전화번호 (실제 번호로 교체)
        String target119 = "01034200339";
        try {
            sendMessage(target119, text);
            log.info("[SMS] {} 전송 성공", label);
            return true;
        } catch (Exception e) {
            log.error("[SMS] {} 전송 실패: {}", label, e.getMessage());
            return false;
        }
    }

    /** CoolSMS 실제 전송 */
    private void sendMessage(String to, String text) throws Exception{
        Message message = new Message();
        message.setFrom(smsProperties.getFromNumber());
        message.setTo(to);
        message.setText(text);
        message.setSubject("[늘봄 긴급알림]");
        messageService.send(message);
    }

    /** 119 신고 문자 내용 조립 */
    private String build119Message(Event event, Resident resident, String reporterPhone) {
        int age = Period.between(resident.getBirthDate(), LocalDate.now()).getYears();
        return String.format(
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
    }

    private Resident findResident(Long residentId) {
        return residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("거주자 없음: " + residentId));
    }

    private void saveSmsLog(Long eventId, String phone, String text,
                            String smsType, boolean success) {
        try {
            smsLogRepository.save(SmsLog.builder()
                    .eventId(eventId)
                    .recipientPhone(phone)
                    .message(text)
                    .smsType(smsType)
                    .success(success)
                    .build());
            log.info("[SMS_LOG 저장] eventId={} type={} success={}", eventId, smsType, success);
        } catch (Exception e) {
            log.error("[SMS_LOG 저장 실패] eventId={} 오류: {}", eventId, e.getMessage(), e);
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // 119 자동신고 재시도 (지수백오프 x6)
    public boolean sendAutoReportWithRetry(Event event, int maxRetry) {
        Resident resident = findResident(event.getResidentId());
        List<Member> families = memberRepository.findAllByResidentId(event.getResidentId());
        String reporterPhone = families.isEmpty() ? "미등록" : families.get(0).getPhone();
        String message = build119Message(event, resident, reporterPhone);

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                sendMessage("01034200339", message); // 119 번호로 교체
                log.info("[SMS] 119 재시도 성공 ({}회차)", attempt);
                return true;
            } catch (Exception e) {
                long waitMs = (long) Math.pow(2, attempt) * 1000;
                log.warn("[SMS] 119 재시도 실패 ({}회차), {}ms 대기", attempt, waitMs);
                sleep(waitMs);
            }
        }
        return false;
    }

}