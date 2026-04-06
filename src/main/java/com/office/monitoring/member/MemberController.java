package com.office.monitoring.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
/** MemberController의 역할을 담당한다. */
public class MemberController {

    @GetMapping({"/login", ""})
    /** login 동작을 수행한다. */
    public String login() {
        return "member/login";
    }

    @GetMapping("/register")
    /** register 동작을 수행한다. */
    public String register() {
        return "member/register";
    }

    @GetMapping("/withdraw")
    /** withdraw 동작을 수행한다. */
    public String withdraw() {
        return "member/withdraw";
    }
}