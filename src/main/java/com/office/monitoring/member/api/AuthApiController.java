package com.office.monitoring.member.api;

import com.office.monitoring.member.MemberService;
import com.office.monitoring.member.dto.CheckUsernameResponse;
import com.office.monitoring.member.dto.RegisterRequest;
import com.office.monitoring.member.dto.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return memberService.register(request);
    }
}
