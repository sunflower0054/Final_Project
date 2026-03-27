package com.office.monitoring.index;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"", "/", "/index", "/index/index"})
    public String index() {
        return "index/index";
    }

    @GetMapping({"/camera", "/camera/camera"})
    public String camera() {
        return "camera/camera";
    }

    @GetMapping({"/test"})
    public String test() {
        return "send_message/test";
    }
}
