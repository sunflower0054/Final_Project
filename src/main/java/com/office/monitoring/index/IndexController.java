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

    @GetMapping("/setting")  // 경로 수정
    public String setting() {
        return "setting/setting";
    }

//<script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=e9d7e2bd6e01da3c13623c221568aed7&libraries=services"></script>

    @GetMapping("/test")  // 경로 수정
    public String test() {
        return "/send_message/test";
    }

}
