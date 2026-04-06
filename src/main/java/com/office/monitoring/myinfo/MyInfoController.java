package com.office.monitoring.myinfo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/myinfo")
/** MyInfoController의 역할을 담당한다. */
public class MyInfoController {

    @GetMapping({"", "/myinfo"})
    /** myinfo 동작을 수행한다. */
    public String myinfo() {
        return "myinfo/myinfo";
    }
}
