package com.office.monitoring.withdrawnUser;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // RestController가 아닌 일반 Controller
public class WithdrawViewController {

    @GetMapping("/member/withdraw")
    public String withdrawPage() {
        // src/main/resources/templates/member/withdraw.html 반환
        return "member/withdraw";
    }
}