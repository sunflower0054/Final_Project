package com.office.monitoring.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
/** 회원 정보 조회, 가입, 수정, 탈퇴와 관련된 도메인 동작을 담당하는 구성 요소. */
public class MemberController {

    @GetMapping({"/login", ""})
    /** 인증 상태를 갱신하고 클라이언트가 사용할 로그인/로그아웃 결과를 반환한다. */
    public String login() {
        return "member/login";
    }

    @GetMapping("/register")
    /** 요청 데이터를 회원 기준으로 저장하고 저장 결과를 반환한다. */
    public String register() {
        return "member/register";
    }

    @GetMapping("/withdraw")
    /** 대상 회원 정보를 삭제 또는 탈퇴 처리하고 완료 결과를 반환한다. */
    public String withdraw() {
        return "member/withdraw";
    }
}
