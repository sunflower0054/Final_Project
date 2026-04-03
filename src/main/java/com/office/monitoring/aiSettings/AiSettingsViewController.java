package com.office.monitoring.aiSettings;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/setting")
public class AiSettingsViewController {

    @GetMapping("/setting")
    public String settingPage() {
        return "setting/setting";   // templates/setting/setting.html 경로
    }
}
