package com.office.monitoring.facility;

import com.office.monitoring.resident.Resident;
import com.office.monitoring.resident.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor // Repository 주입을 위해 추가
public class FacilityController {

    private final ResidentRepository residentRepository; // DB 접근을 위한 Repository

    @GetMapping("/facility/search")
    public String searchPage(@RequestParam(name = "residentId") Long residentId, Model model) {
        // 1. DB에서 거주자 조회 (Repository 필수!)
        Resident resident = residentRepository.findById(residentId)
                .orElse(null); // 에러를 터뜨리지 않고 null 반환

        if (resident == null) {
            // 거주자가 없을 경우 index로 리다이렉트하거나 에러 메시지 처리
            return "redirect:/index?error=no_resident";
        }

        // 2. 중요: 'resident'라는 이름으로 모델에 꼭 담아주어야 합니다!
        model.addAttribute("resident", resident);

        return "facility/search";
    }
}