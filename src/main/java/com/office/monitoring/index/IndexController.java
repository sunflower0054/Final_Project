package com.office.monitoring.index;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession; // 세션 사용을 위해 필요

@Controller
public class IndexController {

    @GetMapping({"", "/", "/index", "/index/index"})
    public String index() {
        return "index/index";
    }

    @GetMapping({"/test"})
    public String test() {
        return "send_message/test";
    }

}