package com.office.monitoring.myinfo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/myinfo")
/** 로그인한 사용자의 내 정보 화면 이동을 담당하는 컨트롤러. */
public class MyInfoController {

    @GetMapping({"", "/myinfo"})
    /** 요청된 내 정보 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public String myinfo() {
        return "myinfo/myinfo";
    }
}
