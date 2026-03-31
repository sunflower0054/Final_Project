package com.office.monitoring.member;

import com.office.monitoring.member.dto.CheckUsernameResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final MemberService memberService;

    @GetMapping("/check-username")
    public CheckUsernameResponse checkUsername(@RequestParam String username) {
        boolean available = memberService.checkUsernameAvailable(username);

        if (available) {
            return new CheckUsernameResponse(true, true, "사용 가능한 아이디입니다.");
        }

        return new CheckUsernameResponse(true, false, "이미 사용 중인 아이디입니다.");
    }
}
