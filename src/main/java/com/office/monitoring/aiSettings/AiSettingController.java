package com.office.monitoring.aiSettings;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/setting")
public class AiSettingController {

    @GetMapping({"", "/setting"})
    public String setting() {
        return "setting/setting";
    }
}
