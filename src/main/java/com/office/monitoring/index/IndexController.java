package com.office.monitoring.index;

import com.office.monitoring.resident.Resident;
import com.office.monitoring.resident.ResidentRepository;
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

    @GetMapping({"", "/", "/index", "/index/index"})
    public String index(@RequestParam(name = "residentId", required = false) Long residentId, Model model) {

        Resident resident = null;

        // 1. 파라미터로 ID가 넘어온 경우
        if (residentId != null) {
            // orElseThrow 대신 orElse(null)을 사용하여 에러를 막습니다.
            resident = residentRepository.findById(residentId).orElse(null);
        }
        // 2. 파라미터 없이 그냥 접속한 경우 (예: 기본으로 첫 번째 사람을 보여줌)
        else {
            List<Resident> residents = residentRepository.findAll();
            if (!residents.isEmpty()) {
                resident = residents.get(0); // DB의 가장 첫 번째 어르신
            }
        }

        // 3. 최종적으로 어르신 정보가 DB에 하나도 없거나, 잘못된 ID인 경우
        if (resident == null) {
            model.addAttribute("errorMessage", "⚠️ 회원정보를 불러올 수 없습니다. 등록된 어르신이 있는지 확인해 주세요.");
        } else {
            model.addAttribute("resident", resident);
        }

        return "index/index";
    }

    @GetMapping({"/test"})
    public String test() {
        return "send_message/test";
    }
}