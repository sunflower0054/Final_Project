package com.office.monitoring.member.api;

import com.office.monitoring.member.MemberService;
import com.office.monitoring.member.dto.CheckUsernameResponse;
import com.office.monitoring.member.dto.RegisterRequest;
import com.office.monitoring.member.dto.RegisterResponse;
import com.office.monitoring.member.dto.WithdrawRequest;
import com.office.monitoring.member.dto.WithdrawResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
/** 회원 관련 HTTP 요청을 받아 JSON 응답으로 반환하는 API 컨트롤러. */
public class AuthApiController {

    private final MemberService memberService;

    @GetMapping("/check-username")
    /** 입력된 값이 회원 규칙을 만족하는지 판별해 사용 가능 여부를 반환한다. */
    public CheckUsernameResponse checkUsername(@RequestParam String username) {
        boolean available = memberService.checkUsernameAvailable(username);

        if (available) {
            return new CheckUsernameResponse(true, true, "사용 가능한 아이디입니다.");
        }

        return new CheckUsernameResponse(true, false, "이미 사용 중인 아이디입니다.");
    }

    @PostMapping("/register")
    /** 요청 데이터를 회원 기준으로 저장하고 저장 결과를 반환한다. */
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return memberService.register(request);
    }

    @DeleteMapping("/withdraw")
    public WithdrawResponse withdraw(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @Valid @RequestBody WithdrawRequest withdrawRequest) {
        WithdrawResponse result = memberService.withdraw(withdrawRequest);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return result;
    }
}
