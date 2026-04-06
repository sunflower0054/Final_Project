package com.office.monitoring.index;

import com.office.monitoring.resident.Resident;
import com.office.monitoring.resident.ResidentRepository;
import com.office.monitoring.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final ResidentRepository residentRepository;
    private final CurrentUserService currentUserService; // 1. CurrentUserService 주입 추가

    @GetMapping({"", "/", "/index", "/index/index"})
    public String index(@RequestParam(name = "residentId", required = false) Long residentId, Model model) {

        Resident resident = null;

        // 1. 주소창에 직접 id를 치고 들어온 경우 (관리자 확인용 등)
        if (residentId != null) {
            resident = residentRepository.findById(residentId).orElse(null);
        }
        // 2. 주소창에 id가 없는 경우 (일반적인 접속)
        else {
            try {
                // 2. 로그인한 사용자의 residentId를 가져옵니다.
                Long loggedInResidentId = currentUserService.getResidentId();
                resident = residentRepository.findById(loggedInResidentId).orElse(null);
            } catch (Exception e) {
                // 로그인 정보가 없거나 거주자 등록이 안 된 경우
                // 로그인을 유도하거나 등록 페이지로 보낼 수 있습니다.
                return "redirect:/member/login";
            }
        }

        if (resident == null) {
            model.addAttribute("errorMessage", "⚠️ 거주자 정보를 찾을 수 없습니다.");
            return "index/index";
        }

        model.addAttribute("resident", resident);
        return "index/index";
    }
}