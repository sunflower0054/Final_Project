package com.office.monitoring.index;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"", "/"})
    public String index() {
        return "index/index";
    }

    @GetMapping("/index")  // 추가
    public String indexPage() {
        return "index/index";
    }

    @GetMapping("/camera")  // 추가
    public String camera() {
        return "camera/camera";
    }

    @GetMapping("/event")  // 오타 수정 + 경로 수정
    public String event() {
        return "event/event";
    }

    @GetMapping("/status")  // 경로 수정
    public String status() {
        return "status/status";
    }

    @GetMapping("setting")  // 경로 수정
    public String setting() {
        return "setting/setting";
    }



}
