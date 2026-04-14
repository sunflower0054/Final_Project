package com.office.monitoring.aiSettings;

import com.office.monitoring.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/setting")
@RequiredArgsConstructor
public class AiSettingsViewController {

    private final CurrentUserService currentUserService;

    @GetMapping("/setting")
    public String settingPage(Model model) {
        Long currentResidentId = currentUserService.getResidentId();   // ← 이렇게 변경
        model.addAttribute("residentId", currentResidentId);
        return "setting/setting";   // templates/setting/setting.html
    }
}
