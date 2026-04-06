package com.office.monitoring.myinfo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/myinfo")
public class MyInfoController {

    @GetMapping({"", "/myinfo"})
    public String myinfo() {
        return "myinfo/myinfo";
    }
}
