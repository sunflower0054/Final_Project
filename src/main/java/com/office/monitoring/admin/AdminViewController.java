package com.office.monitoring.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping("/stats")
    public String showAdminStatsPage() {
        return "admin/adminStats";
    }
}
