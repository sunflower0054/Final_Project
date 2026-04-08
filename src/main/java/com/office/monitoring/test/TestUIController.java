package com.office.monitoring.test;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class TestUIController {

    // 공통 가짜 데이터 생성 메서드 (중복 코드 방지)
    private Map<String, Object> getMockResident() {
        Map<String, Object> resident = new HashMap<>();
        resident.put("name", "김늘봄");
        resident.put("age", 75);
        resident.put("disease", "고혈압, 당뇨, 거동 불편");
        resident.put("address", "대전광역시 서구 둔산대로 135"); // 실제 지도 확인을 위한 주소
        resident.put("phone", "010-1234-5678");
        resident.put("guardianPhone", "010-9876-5432");
        return resident;
    }

    // 1. 자동 신고 완료 페이지 테스트
    @GetMapping("/test/auto")
    public String testAutoPage(Model model) {
        model.addAttribute("resident", getMockResident());
        return "report/autoReport";
    }

    // 2. 신고 실패 페이지 테스트
    @GetMapping("/test/fail")
    public String testFailPage(Model model) {
        model.addAttribute("resident", getMockResident());
        return "report/failReport";
    }

    // 3. 수동 신고 완료 페이지 테스트
    @GetMapping("/test/manual")
    public String testManualPage(Model model) {
        model.addAttribute("resident", getMockResident());
        return "report/manuReport";
    }
}