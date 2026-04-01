package com.office.monitoring.resident;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/resident")
public class ResidentController {

    @GetMapping("/detail")
    public String residentDetail() {
        return "resident/resident_detail";
    }

    @GetMapping("/list")
    public String residentList() {
        return "resident/resident_list";
    }

    @GetMapping("/edit")
    public String residentEdit() {
        return "resident/resident_edit";
    }

    @GetMapping("/register")
    public String residentRegister() {
        return "resident/resident_register";
    }
}
